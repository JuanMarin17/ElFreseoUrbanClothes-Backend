const express = require('express');
const multer = require('multer');
const sharp = require('sharp');
const { v2: cloudinary } = require('cloudinary');
require('dotenv').config();

cloudinary.config({
  cloud_name: process.env.NAME_CLOUD,
  api_key: process.env.KEY_CLOUD,
  api_secret: process.env.SECRET_CLOUD,
});

const ALLOWED_MIME_TYPES = ['image/jpeg', 'image/png', 'image/webp', 'image/gif'];
const MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB

const app = express();
const upload = multer({
  storage: multer.memoryStorage(),
  limits: { fileSize: MAX_FILE_SIZE },
  fileFilter: (_req, file, cb) => {
    if (ALLOWED_MIME_TYPES.includes(file.mimetype)) {
      cb(null, true);
    } else {
      cb(new Error(`Tipo de archivo no permitido: ${file.mimetype}. Solo se aceptan imágenes.`));
    }
  },
});

const FOLDER_PATTERN = /^[a-zA-Z0-9/_-]{1,100}$/;

const resolveFolder = (rawFolder) => {
  const folder = rawFolder || 'general';
  if (!FOLDER_PATTERN.test(folder)) {
    return null;
  }
  return folder;
};

const compressToWebP = (buffer, mimetype) => {
  const isAnimated = mimetype === 'image/gif';
  return sharp(buffer, { animated: isAnimated })
    .webp({ quality: 82, effort: 4 })
    .toBuffer();
};

const uploadToCloudinary = (buffer, folder) =>
  new Promise((resolve, reject) => {
    cloudinary.uploader
      .upload_stream({ folder, resource_type: 'image', format: 'webp' }, (error, result) => {
        if (error) reject(error);
        else resolve(result);
      })
      .end(buffer);
  });

const handleUpload = async (req, res) => {
  if (!req.file) {
    return res.status(400).json({ error: 'No se recibió ningún archivo' });
  }

  const folder = resolveFolder(req.query.folder);
  if (!folder) {
    return res.status(400).json({ error: 'Parámetro "folder" inválido' });
  }

  try {
    const webpBuffer = await compressToWebP(req.file.buffer, req.file.mimetype);
    const result = await uploadToCloudinary(webpBuffer, folder);
    res.json({ url: result.secure_url });
  } catch (error) {
    console.error('Error al procesar/subir imagen:', error.message);
    res.status(500).json({ error: 'Error al procesar imagen: ' + error.message });
  }
};
app.get('/health', (_req, res) => res.json({ status: 'UP' }));

// POST /api/v1/upload?folder=general
app.post('/api/v1/upload', upload.single('image'), handleUpload);

// Alias: el Gateway redirige /api/v1/cloudinary/** a este servicio
app.post('/api/v1/cloudinary/upload', upload.single('image'), handleUpload);

const handleSignature = (req, res) => {
  const folder = resolveFolder(req.query.folder);
  if (!folder) {
    return res.status(400).json({ error: 'Parámetro "folder" inválido' });
  }
  const timestamp = Math.round(Date.now() / 1000);

  const signature = cloudinary.utils.api_sign_request(
    { timestamp, folder },
    process.env.SECRET_CLOUD
  );

  res.json({
    signature,
    timestamp,
    cloudName: process.env.NAME_CLOUD,
    apiKey: process.env.KEY_CLOUD,
    folder,
  });
};

// GET /api/v1/upload/signature?folder=general
app.get('/api/v1/upload/signature', handleSignature);

// Alias: el Gateway redirige /api/v1/cloudinary/** a este servicio
app.get('/api/v1/cloudinary/signature', handleSignature);

// Captura errores de multer (tipo de archivo no permitido, archivo demasiado
// grande, etc.) que llegan vía cb(err) antes de entrar a handleUpload, y que
// de otro modo caerían en el manejador de errores HTML por defecto de Express.
app.use((err, _req, res, _next) => {
  if (err instanceof multer.MulterError && err.code === 'LIMIT_FILE_SIZE') {
    return res.status(400).json({ error: 'El archivo supera el tamaño máximo permitido (10 MB).' });
  }
  if (err) {
    return res.status(400).json({ error: err.message ?? 'Error al procesar el archivo.' });
  }
  _next();
});

const PORT = process.env.PORT || 8096;
app.listen(PORT, () => {
  console.log(`Media service corriendo en http://localhost:${PORT}`);
});
