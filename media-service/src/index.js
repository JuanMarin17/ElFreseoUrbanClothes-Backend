const express = require('express');
const multer = require('multer');
const { v2: cloudinary } = require('cloudinary');
require('dotenv').config();

cloudinary.config({
  cloud_name: process.env.NAME_CLOUD,
  api_key: process.env.KEY_CLOUD,
  api_secret: process.env.SECRET_CLOUD,
});

const app = express();
const upload = multer({ storage: multer.memoryStorage() });

const uploadToCloudinary = (buffer, folder) =>
  new Promise((resolve, reject) => {
    cloudinary.uploader
      .upload_stream({ folder, resource_type: 'image' }, (error, result) => {
        if (error) reject(error);
        else resolve(result);
      })
      .end(buffer);
  });

const handleUpload = async (req, res) => {
  if (!req.file) {
    return res.status(400).json({ error: 'No se recibió ningún archivo' });
  }

  const folder = req.query.folder || 'general';

  try {
    const result = await uploadToCloudinary(req.file.buffer, folder);
    res.json({ url: result.secure_url });
  } catch (error) {
    console.error('Error al subir a Cloudinary:', error.message);
    res.status(500).json({ error: 'Error al subir imagen: ' + error.message });
  }
};
// POST /api/v1/upload?folder=general
app.post('/api/v1/upload', upload.single('image'), handleUpload);

// Alias: el Gateway redirige /api/v1/cloudinary/** a este servicio
app.post('/api/v1/cloudinary/upload', upload.single('image'), handleUpload);

const handleSignature = (req, res) => {
  const folder = req.query.folder || 'general';
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

const PORT = process.env.PORT || 8096;
app.listen(PORT, () => {
  console.log(`Media service corriendo en http://localhost:${PORT}`);
});
