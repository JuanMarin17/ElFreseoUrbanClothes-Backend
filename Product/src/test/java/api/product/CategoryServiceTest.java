package api.product;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.api.product.dto.CategoryRequestDTO;
import com.api.product.dto.CategoryResponseDTO;
import com.api.product.entity.Category;
import com.api.product.repository.CategoryRepository;
import com.api.product.service.CategoryService;

class CategoryServiceTest {

    private CategoryRepository categoryRepository;
    private CategoryService categoryService;

    @BeforeEach
    void setUp() {
        categoryRepository = Mockito.mock(CategoryRepository.class);
        categoryService = new CategoryService(categoryRepository);
    }

    // -----------------------------
    // Helpers
    // -----------------------------

    private CategoryRequestDTO buildValidDTO() {
        CategoryRequestDTO dto = new CategoryRequestDTO();
        dto.setName("Camisetas");
        dto.setActive(true);
        return dto;
    }

    private Category buildCategoryEntity(UUID id, String name, boolean active) {
        return Category.builder()
                .categoryId(id)
                .name(name)
                .active(active)
                .build();
    }

    // -----------------------------
    // CREATE CATEGORY TESTS
    // -----------------------------

    @Test
    void createCategory_shouldThrowException_whenDtoIsNull() {

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> categoryService.createCategory(null));

        assertTrue(ex.getMessage().contains("Error al crear categoría"));
    }

    @Test
    void createCategory_shouldThrowException_whenNameIsBlank() {

        CategoryRequestDTO dto = buildValidDTO();
        dto.setName("   ");

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> categoryService.createCategory(dto));

        assertTrue(ex.getMessage().contains("Error al crear categoría"));
    }

    @Test
    void createCategory_shouldThrowException_whenCategoryAlreadyExists() {

        CategoryRequestDTO dto = buildValidDTO();

        when(categoryRepository.existsByNameIgnoreCase(dto.getName()))
                .thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> categoryService.createCategory(dto));

        assertTrue(ex.getMessage().contains("La categoría ya existe"));
    }

    @Test
    void createCategory_shouldCreateSuccessfully() {

        CategoryRequestDTO dto = buildValidDTO();

        when(categoryRepository.existsByNameIgnoreCase(dto.getName()))
                .thenReturn(false);

        Category saved = buildCategoryEntity(UUID.randomUUID(), dto.getName(), true);

        when(categoryRepository.save(any(Category.class)))
                .thenReturn(saved);

        CategoryResponseDTO response = categoryService.createCategory(dto);

        assertNotNull(response);
        assertEquals(dto.getName(), response.getName());
        assertEquals("ACTIVE", response.getStatus());

        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    void createCategory_shouldSetActiveTrue_whenActiveIsNull() {

        CategoryRequestDTO dto = buildValidDTO();
        dto.setActive(null);

        when(categoryRepository.existsByNameIgnoreCase(dto.getName()))
                .thenReturn(false);

        when(categoryRepository.save(any(Category.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CategoryResponseDTO response = categoryService.createCategory(dto);

        assertNotNull(response);
        assertEquals("Camisetas", response.getName());

        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    // -----------------------------
    // UPDATE CATEGORY TESTS
    // -----------------------------

    @Test
    void updateCategory_shouldReturnEmpty_whenCategoryNotFound() {

        UUID id = UUID.randomUUID();
        CategoryRequestDTO dto = buildValidDTO();

        when(categoryRepository.findById(id))
                .thenReturn(Optional.empty());

        Optional<CategoryResponseDTO> result = categoryService.updateCategory(id, dto);

        assertTrue(result.isEmpty());
    }

    @Test
    void updateCategory_shouldThrowException_whenNameIsBlank() {

        UUID id = UUID.randomUUID();

        CategoryRequestDTO dto = buildValidDTO();
        dto.setName("   ");

        Category existing = buildCategoryEntity(id, "Pantalones", true);

        when(categoryRepository.findById(id))
                .thenReturn(Optional.of(existing));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> categoryService.updateCategory(id, dto));

        assertTrue(ex.getMessage().contains("Error al actualizar categoría"));
    }

    @Test
    void updateCategory_shouldThrowException_whenNameAlreadyExists() {

        UUID id = UUID.randomUUID();

        CategoryRequestDTO dto = buildValidDTO();
        dto.setName("Camisetas");

        Category existing = buildCategoryEntity(id, "Pantalones", true);

        when(categoryRepository.findById(id))
                .thenReturn(Optional.of(existing));

        when(categoryRepository.existsByNameIgnoreCase("Camisetas"))
                .thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> categoryService.updateCategory(id, dto));

        assertTrue(ex.getMessage().contains("Ya existe otra categoría con el mismo nombre"));
    }

    @Test
    void updateCategory_shouldUpdateSuccessfully() {

        UUID id = UUID.randomUUID();

        CategoryRequestDTO dto = buildValidDTO();
        dto.setName("Camisetas");

        Category existing = buildCategoryEntity(id, "Pantalones", true);

        when(categoryRepository.findById(id))
                .thenReturn(Optional.of(existing));

        when(categoryRepository.existsByNameIgnoreCase(dto.getName()))
                .thenReturn(false);

        when(categoryRepository.save(any(Category.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Optional<CategoryResponseDTO> result = categoryService.updateCategory(id, dto);

        assertTrue(result.isPresent());
        assertEquals("Camisetas", result.get().getName());
        assertEquals("ACTIVE", result.get().getStatus());

        verify(categoryRepository, times(1)).save(existing);
    }

    @Test
    void updateCategory_shouldUpdateActive_whenActiveIsProvided() {

        UUID id = UUID.randomUUID();

        CategoryRequestDTO dto = buildValidDTO();
        dto.setActive(false);

        Category existing = buildCategoryEntity(id, "Pantalones", true);

        when(categoryRepository.findById(id))
                .thenReturn(Optional.of(existing));

        when(categoryRepository.existsByNameIgnoreCase(dto.getName()))
                .thenReturn(false);

        when(categoryRepository.save(any(Category.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Optional<CategoryResponseDTO> result = categoryService.updateCategory(id, dto);

        assertTrue(result.isPresent());
        assertEquals("INACTIVE", result.get().getStatus());
    }

    // -----------------------------
    // LIST METHODS TESTS
    // -----------------------------

    @Test
    void listAllCategories_shouldReturnList() {

        Category c1 = buildCategoryEntity(UUID.randomUUID(), "Camisetas", true);
        Category c2 = buildCategoryEntity(UUID.randomUUID(), "Pantalones", false);

        when(categoryRepository.findAll())
                .thenReturn(List.of(c1, c2));

        List<CategoryResponseDTO> result = categoryService.listAllCategories();

        assertEquals(2, result.size());
    }

    @Test
    void listAllActiveCategories_shouldReturnList() {

        Category c1 = buildCategoryEntity(UUID.randomUUID(), "Camisetas", true);
        Category c2 = buildCategoryEntity(UUID.randomUUID(), "Gorras", true);

        when(categoryRepository.findByActiveTrue())
                .thenReturn(List.of(c1, c2));

        List<CategoryResponseDTO> result = categoryService.listAllActiveCategories();

        assertEquals(2, result.size());
        assertEquals("Camisetas", result.get(0).getName());
    }

    // -----------------------------
    // GET BY ID TESTS
    // -----------------------------

    @Test
    void getById_shouldReturnCategory_whenFound() {

        UUID id = UUID.randomUUID();
        Category category = buildCategoryEntity(id, "Camisetas", true);

        when(categoryRepository.findById(id))
                .thenReturn(Optional.of(category));

        Optional<CategoryResponseDTO> result = categoryService.getById(id);

        assertTrue(result.isPresent());
        assertEquals("Camisetas", result.get().getName());
    }

    @Test
    void getById_shouldReturnEmpty_whenNotFound() {

        UUID id = UUID.randomUUID();

        when(categoryRepository.findById(id))
                .thenReturn(Optional.empty());

        Optional<CategoryResponseDTO> result = categoryService.getById(id);

        assertTrue(result.isEmpty());
    }

    // -----------------------------
    // ACTIVE / INACTIVE TESTS
    // -----------------------------

    @Test
    void inactiveCategory_shouldReturnEmpty_whenNotFound() {

        UUID id = UUID.randomUUID();

        when(categoryRepository.findById(id))
                .thenReturn(Optional.empty());

        Optional<CategoryResponseDTO> result = categoryService.inactiveCategory(id);

        assertTrue(result.isEmpty());
    }

    @Test
    void inactiveCategory_shouldInactiveSuccessfully() {

        UUID id = UUID.randomUUID();
        Category category = buildCategoryEntity(id, "Camisetas", true);

        when(categoryRepository.findById(id))
                .thenReturn(Optional.of(category));

        Optional<CategoryResponseDTO> result = categoryService.inactiveCategory(id);

        assertTrue(result.isPresent());
        assertEquals("INACTIVE", result.get().getStatus());

        verify(categoryRepository, times(1)).save(category);
    }

    @Test
    void activeCategory_shouldReturnEmpty_whenNotFound() {

        UUID id = UUID.randomUUID();

        when(categoryRepository.findById(id))
                .thenReturn(Optional.empty());

        Optional<CategoryResponseDTO> result = categoryService.activeCategory(id);

        assertTrue(result.isEmpty());
    }

    @Test
    void activeCategory_shouldActiveSuccessfully() {

        UUID id = UUID.randomUUID();
        Category category = buildCategoryEntity(id, "Camisetas", false);

        when(categoryRepository.findById(id))
                .thenReturn(Optional.of(category));

        Optional<CategoryResponseDTO> result = categoryService.activeCategory(id);

        assertTrue(result.isPresent());
        assertEquals("ACTIVE", result.get().getStatus());

        verify(categoryRepository, times(1)).save(category);
    }
}