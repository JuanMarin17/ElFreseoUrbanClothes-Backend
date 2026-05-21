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

import com.api.product.dto.BrandRequestDTO;
import com.api.product.dto.BrandResponseDTO;
import com.api.product.entity.Brand;
import com.api.product.repository.BrandRepository;
import com.api.product.service.BrandService;

class BrandServiceTest {

    private BrandRepository brandRepository;
    private BrandService brandService;

    @BeforeEach
    void setUp() {
        brandRepository = Mockito.mock(BrandRepository.class);
        brandService = new BrandService(brandRepository);
    }

    // -----------------------------
    // Helpers
    // -----------------------------

    private BrandRequestDTO buildValidDTO() {
        BrandRequestDTO dto = new BrandRequestDTO();
        dto.setName("Nike");
        dto.setActive(true);
        return dto;
    }

    private Brand buildBrandEntity(UUID id, String name, boolean active) {
        return Brand.builder()
                .brandId(id)
                .name(name)
                .active(active)
                .build();
    }

    // -----------------------------
    // CREATE BRAND TESTS
    // -----------------------------

    @Test
    void createBrand_shouldThrowException_whenDtoIsNull() {

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> brandService.createBrand(null));

        assertTrue(ex.getMessage().contains("Error al crear marca"));
    }

    @Test
    void createBrand_shouldThrowException_whenNameIsBlank() {

        BrandRequestDTO dto = buildValidDTO();
        dto.setName("   ");

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> brandService.createBrand(dto));

        assertTrue(ex.getMessage().contains("Error al crear marca"));
    }

    @Test
    void createBrand_shouldThrowException_whenBrandAlreadyExists() {

        BrandRequestDTO dto = buildValidDTO();

        when(brandRepository.existsByNameIgnoreCase(dto.getName()))
                .thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> brandService.createBrand(dto));

        assertTrue(ex.getMessage().contains("La marca ya existe"));
    }

    @Test
    void createBrand_shouldCreateSuccessfully() {

        BrandRequestDTO dto = buildValidDTO();

        when(brandRepository.existsByNameIgnoreCase(dto.getName()))
                .thenReturn(false);

        Brand saved = buildBrandEntity(UUID.randomUUID(), dto.getName(), true);

        when(brandRepository.save(any(Brand.class)))
                .thenReturn(saved);

        BrandResponseDTO response = brandService.createBrand(dto);

        assertNotNull(response);
        assertEquals(dto.getName(), response.getName());
        assertEquals("ACTIVE", response.getStatus());

        verify(brandRepository, times(1)).save(any(Brand.class));
    }

    @Test
    void createBrand_shouldSetActiveTrue_whenActiveIsNull() {

        BrandRequestDTO dto = buildValidDTO();
        dto.setActive(null);

        when(brandRepository.existsByNameIgnoreCase(dto.getName()))
                .thenReturn(false);

        when(brandRepository.save(any(Brand.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        BrandResponseDTO response = brandService.createBrand(dto);

        assertNotNull(response);
        assertEquals("Nike", response.getName());

        verify(brandRepository, times(1)).save(any(Brand.class));
    }

    // -----------------------------
    // UPDATE BRAND TESTS
    // -----------------------------

    @Test
    void updateBrand_shouldReturnEmpty_whenBrandNotFound() {

        UUID id = UUID.randomUUID();
        BrandRequestDTO dto = buildValidDTO();

        when(brandRepository.findById(id))
                .thenReturn(Optional.empty());

        Optional<BrandResponseDTO> result = brandService.updateBrand(id, dto);

        assertTrue(result.isEmpty());
    }

    @Test
    void updateBrand_shouldThrowException_whenNameIsBlank() {

        UUID id = UUID.randomUUID();

        BrandRequestDTO dto = buildValidDTO();
        dto.setName("   ");

        Brand existing = buildBrandEntity(id, "Adidas", true);

        when(brandRepository.findById(id))
                .thenReturn(Optional.of(existing));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> brandService.updateBrand(id, dto));

        assertTrue(ex.getMessage().contains("Error al actualizar marca"));
    }

    @Test
    void updateBrand_shouldThrowException_whenNameAlreadyExists() {

        UUID id = UUID.randomUUID();

        BrandRequestDTO dto = buildValidDTO();
        dto.setName("Nike");

        Brand existing = buildBrandEntity(id, "Adidas", true);

        when(brandRepository.findById(id))
                .thenReturn(Optional.of(existing));

        when(brandRepository.existsByNameIgnoreCase("Nike"))
                .thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> brandService.updateBrand(id, dto));

        assertTrue(ex.getMessage().contains("Ya existe otra marca con el mismo nombre"));
    }

    @Test
    void updateBrand_shouldUpdateSuccessfully() {

        UUID id = UUID.randomUUID();

        BrandRequestDTO dto = buildValidDTO();
        dto.setName("Nike");

        Brand existing = buildBrandEntity(id, "Adidas", true);

        when(brandRepository.findById(id))
                .thenReturn(Optional.of(existing));

        when(brandRepository.existsByNameIgnoreCase(dto.getName()))
                .thenReturn(false);

        when(brandRepository.save(any(Brand.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Optional<BrandResponseDTO> result = brandService.updateBrand(id, dto);

        assertTrue(result.isPresent());
        assertEquals("Nike", result.get().getName());
        assertEquals("ACTIVE", result.get().getStatus());

        verify(brandRepository, times(1)).save(existing);
    }

    @Test
    void updateBrand_shouldUpdateActive_whenActiveIsProvided() {

        UUID id = UUID.randomUUID();

        BrandRequestDTO dto = buildValidDTO();
        dto.setActive(false);

        Brand existing = buildBrandEntity(id, "Adidas", true);

        when(brandRepository.findById(id))
                .thenReturn(Optional.of(existing));

        when(brandRepository.existsByNameIgnoreCase(dto.getName()))
                .thenReturn(false);

        when(brandRepository.save(any(Brand.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Optional<BrandResponseDTO> result = brandService.updateBrand(id, dto);

        assertTrue(result.isPresent());
        assertEquals("INACTIVE", result.get().getStatus());
    }

    // -----------------------------
    // LIST METHODS TESTS
    // -----------------------------

    @Test
    void listAllBrands_shouldReturnList() {

        Brand b1 = buildBrandEntity(UUID.randomUUID(), "Nike", true);
        Brand b2 = buildBrandEntity(UUID.randomUUID(), "Adidas", false);

        when(brandRepository.findAll())
                .thenReturn(List.of(b1, b2));

        List<BrandResponseDTO> result = brandService.listAllBrands();

        assertEquals(2, result.size());
    }

    @Test
    void listAllActiveBrands_shouldReturnList() {

        Brand b1 = buildBrandEntity(UUID.randomUUID(), "Nike", true);
        Brand b2 = buildBrandEntity(UUID.randomUUID(), "Puma", true);

        when(brandRepository.findByActiveTrue())
                .thenReturn(List.of(b1, b2));

        List<BrandResponseDTO> result = brandService.listAllActiveBrands();

        assertEquals(2, result.size());
        assertEquals("Nike", result.get(0).getName());
    }

    // -----------------------------
    // GET BY ID TESTS
    // -----------------------------

    @Test
    void getById_shouldReturnBrand_whenFound() {

        UUID id = UUID.randomUUID();
        Brand brand = buildBrandEntity(id, "Nike", true);

        when(brandRepository.findById(id))
                .thenReturn(Optional.of(brand));

        Optional<BrandResponseDTO> result = brandService.getById(id);

        assertTrue(result.isPresent());
        assertEquals("Nike", result.get().getName());
    }

    @Test
    void getById_shouldReturnEmpty_whenNotFound() {

        UUID id = UUID.randomUUID();

        when(brandRepository.findById(id))
                .thenReturn(Optional.empty());

        Optional<BrandResponseDTO> result = brandService.getById(id);

        assertTrue(result.isEmpty());
    }

    // -----------------------------
    // ACTIVE / INACTIVE TESTS
    // -----------------------------

    @Test
    void inactiveBrand_shouldReturnEmpty_whenNotFound() {

        UUID id = UUID.randomUUID();

        when(brandRepository.findById(id))
                .thenReturn(Optional.empty());

        Optional<BrandResponseDTO> result = brandService.inactiveBrand(id);

        assertTrue(result.isEmpty());
    }

    @Test
    void inactiveBrand_shouldInactiveSuccessfully() {

        UUID id = UUID.randomUUID();
        Brand brand = buildBrandEntity(id, "Nike", true);

        when(brandRepository.findById(id))
                .thenReturn(Optional.of(brand));

        Optional<BrandResponseDTO> result = brandService.inactiveBrand(id);

        assertTrue(result.isPresent());
        assertEquals("INACTIVE", result.get().getStatus());

        verify(brandRepository, times(1)).save(brand);
    }

    @Test
    void activeBrand_shouldReturnEmpty_whenNotFound() {

        UUID id = UUID.randomUUID();

        when(brandRepository.findById(id))
                .thenReturn(Optional.empty());

        Optional<BrandResponseDTO> result = brandService.activeBrand(id);

        assertTrue(result.isEmpty());
    }

    @Test
    void activeBrand_shouldActiveSuccessfully() {

        UUID id = UUID.randomUUID();
        Brand brand = buildBrandEntity(id, "Nike", false);

        when(brandRepository.findById(id))
                .thenReturn(Optional.of(brand));

        Optional<BrandResponseDTO> result = brandService.activeBrand(id);

        assertTrue(result.isPresent());
        assertEquals("ACTIVE", result.get().getStatus());

        verify(brandRepository, times(1)).save(brand);
    }
}