from flask import Flask, request, jsonify
import pdfplumber
import os
import re
import openai  # Ensure you install this: pip install openai

app = Flask(__name__)
UPLOAD_FOLDER = "temp"
os.makedirs(UPLOAD_FOLDER, exist_ok=True)

# Set up OpenAI API Key
openai.api_key = "your_openai_api_key"  # Replace with your actual API key


def extract_text_from_pdf(pdf_path):
    """Extract text from PDF while keeping structure intact."""
    text = ""
    with pdfplumber.open(pdf_path) as pdf:
        for page in pdf.pages:
            extracted = page.extract_text(layout=True)
            if extracted:
                text += extracted + "\n"

    # Clean text: remove excessive spaces and unknown characters
    text = re.sub(r'\s+', ' ', text)  # Normalize spaces
    text = text.encode('ascii', 'ignore').decode()  # Remove non-ASCII characters

    return text.strip()


def format_resume(text):
    """Formats extracted resume text into a structured, readable format."""
    lines = text.split("\n")
    formatted_text = ""

    headers = [
        "Experience", "Education", "Skills", "Certifications",
        "Projects", "Summary", "Contact", "Technical Skills"
    ]

    for i, line in enumerate(lines):
        line = line.strip()
        if not line:
            continue  # Skip empty lines

        # Detect headers and format them properly
        if any(line.lower().startswith(header.lower()) for header in headers):
            formatted_text += f"\n\n{'=' * 60}\n{line.upper().center(60)}\n{'=' * 60}\n\n"
        else:
            formatted_text += f"{line}\n"

    return formatted_text.strip()


@app.route('/upload', methods=['POST'])
def upload_file():
    if 'file' not in request.files:
        return jsonify({"error": "No file uploaded"}), 400

    file = request.files['file']
    if file.filename == '':
        return jsonify({"error": "No selected file"}), 400

    file_path = os.path.join(UPLOAD_FOLDER, file.filename)

    try:
        file.save(file_path)
    except Exception as e:
        return jsonify({"error": f"Failed to save file: {str(e)}"}), 500

    extracted_text = extract_text_from_pdf(file_path)
    if not extracted_text:
        return jsonify({"error": "No text extracted from PDF"}), 400

    formatted_resume = format_resume(extracted_text)

    return jsonify({"formatted_resume": formatted_resume})


@app.route('/chat', methods=['POST'])
def chat():
    data = request.json
    user_message = data.get("message", "").strip()

    if not user_message:
        return jsonify({"error": "Empty message"}), 400

    try:
        # Send user message to OpenAI GPT API
        response = openai.ChatCompletion.create(
            model="gpt-3.5-turbo",  # Use the latest GPT model
            messages=[{"role": "user", "content": user_message}]
        )

        ai_response = response["choices"][0]["message"]["content"]
        return jsonify({"response": ai_response})

    except Exception as e:
        return jsonify({"error": f"AI processing error: {str(e)}"}), 500


if __name__ == '__main__':
    app.run(debug=True)
