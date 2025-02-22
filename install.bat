@echo off
echo 🔹 Installing Python dependencies...
pip install -r requirements.txt

echo 🔹 Checking for Node.js...
where node >nul 2>nul || (
    echo ⚠️ Node.js not found. Installing...
    winget install OpenJS.NodeJS
)

echo 🔹 Installing Node.js packages...
if exist package.json (
    npm install
)

echo 🔹 Checking for MongoDB client...
where mongosh >nul 2>nul || (
    echo ⚠️ MongoDB client not found. Installing...
    winget install MongoDB.DatabaseTools
)

echo 🔹 Checking for JavaFX SDK...
if not exist "C:\javafx-sdk" (
    echo ⚠️ JavaFX SDK not found. Installing...
    curl -O https://gluonhq.com/download/javafx-sdk-latest.zip
    tar -xf javafx-sdk-latest.zip -C C:\javafx-sdk
)

echo ✅ Setup complete! Run 'python app.py' to start the server.
