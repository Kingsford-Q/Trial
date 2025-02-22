const express = require("express");
const cors = require("cors");
const multer = require("multer");
const fs = require("fs");
const axios = require("axios");
const FormData = require("form-data");
const connectDB = require("./db");
require("dotenv").config();

const app = express();
const PORT = process.env.PORT || 5000;

// Middleware
app.use(cors());
app.use(express.json());

// Connect to MongoDB
let db;
connectDB()
  .then((database) => {
    db = database;
  })
  .catch((err) => console.error("❌ DB Connection Failed", err));

// File Upload Middleware
const upload = multer({ dest: "uploads/" });

// 📌 Route: Home
app.get("/", (req, res) => {
  res.send("✅ AI Resume Analyzer API is running...");
});

// 📌 Route: Save a new job description
app.post("/analyze-resume", upload.single("file"), async (req, res) => {
  try {
    const file = req.file;
    console.log("📂 Received File:", file);

    if (!file) {
      return res.status(400).json({ error: "❌ No file uploaded" });
    }

    const pythonAPI = "http://127.0.0.1:5000/analyze-resume"; // Flask API URL

    const formData = new FormData();
    formData.append("file", fs.createReadStream(file.path));

    console.log("🚀 Sending file to Flask...");

    const response = await axios.post(pythonAPI, formData, {
      headers: { "Content-Type": "multipart/form-data" },
    });

    console.log("✅ Flask Response:", response.data);

    // Remove uploaded file after sending to Python API
    fs.unlinkSync(file.path);

    res.json(response.data);
  } catch (error) {
    console.error("❌ Resume analysis failed:", error.message);
    res.status(500).json({ error: "❌ Resume analysis failed" });
  }
});
~

// 📌 Route: Get all job descriptions
app.get("/job-descriptions", async (req, res) => {
  try {
    const collection = db.collection("job_descriptions");
    const jobs = await collection.find({}).toArray();

    res.status(200).json(jobs);
  } catch (error) {
    res.status(500).json({ error: "❌ Failed to fetch job descriptions" });
  }
});

// 📌 Route: Analyze a resume (calls Python API)
app.post("/analyze-resume", upload.single("file"), async (req, res) => {
  try {
    const file = req.file;
    const pythonAPI = "http://127.0.0.1:5000/analyze-resume"; // Flask API URL

    const formData = new FormData();
    formData.append("file", fs.createReadStream(file.path));

    const response = await axios.post(pythonAPI, formData, {
      headers: { "Content-Type": "multipart/form-data" },
    });

    // Remove uploaded file after sending to Python API
    fs.unlinkSync(file.path);

    res.json(response.data);
  } catch (error) {
    res.status(500).json({ error: "❌ Resume analysis failed" });
  }
});

// Start Server
app.listen(PORT, () => console.log(`🚀 Server running on port ${PORT}`));
