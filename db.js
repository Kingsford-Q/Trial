const { MongoClient } = require("mongodb");
require("dotenv").config();

const client = new MongoClient(process.env.MONGODB_URI, {
  useNewUrlParser: true,
  useUnifiedTopology: true,
});

async function connectDB() {
  try {
    await client.connect();
    console.log("✅ Connected to MongoDB Atlas");
    return client.db("resume_analyzer");
  } catch (error) {
    console.error("❌ MongoDB Connection Error:", error);
    setTimeout(() => connectDB(), 5000);
  }
}

module.exports = connectDB;
