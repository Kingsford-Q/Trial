import os
from flask import Flask, request, jsonify
from resume_analysis import analyze_resume  # Import your resume analysis function

app = Flask(__name__)

@app.route('/upload', methods=['POST'])
def upload_file():
    if 'file' not in request.files:
        return jsonify({'error': 'No file provided'}), 400

    file = request.files['file']
    if file.filename == '':
        return jsonify({'error': 'No selected file'}), 400

    # Analyze resume directly without saving
    analysis_result = analyze_resume(file)

    response_data = {
        "message": f"File '{file.filename}' uploaded successfully!",
        "analysis": analysis_result  # Resume analysis output
    }

    return jsonify(response_data), 200

if __name__ == '__main__':
    app.run(debug=True)
