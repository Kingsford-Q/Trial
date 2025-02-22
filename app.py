from flask import Flask, request, jsonify
from flask_cors import CORS
import pdfplumber

app = Flask(__name__)
CORS(app)

@app.route("/upload", methods=["POST"])
def upload_resume():
    if "file" not in request.files:
        return jsonify({"error": "No file uploaded"}), 400

    file = request.files["file"]
    
    try:
        with pdfplumber.open(file) as pdf:
            text = ""
            for page in pdf.pages:
                text += page.extract_text() or ""  # Extract text safely

        if not text.strip():
            return jsonify({"error": "No text extracted from PDF"}), 400  # Handle empty text case

        return jsonify({"message": "File processed", "extracted_text": text})

    except Exception as e:
        return jsonify({"error": f"Error processing PDF: {str(e)}"}), 500

if __name__ == "__main__":
    app.run(debug=True)
