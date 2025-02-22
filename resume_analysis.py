import spacy
import fitz  # PyMuPDF for PDF text extraction

# Load spaCy model
nlp = spacy.load("en_core_web_sm")

def extract_text_from_pdf(pdf_file):
    """Extracts text from a PDF file."""
    try:
        doc = fitz.open(stream=pdf_file.read(), filetype="pdf")
        text = "\n".join([page.get_text("text") for page in doc])
        return text.strip()
    except Exception as e:
        return f"Error extracting text: {str(e)}"

def analyze_resume(file):
    """Processes the resume using NLP and extracts key information."""
    text = extract_text_from_pdf(file)

    if not text:
        return {"error": "No text extracted from PDF"}

    # Process text with spaCy
    doc = nlp(text)

    # Extract named entities (skills, locations, etc.)
    extracted_entities = {ent.label_: [] for ent in doc.ents}
    for ent in doc.ents:
        extracted_entities[ent.label_].append(ent.text)

    # Create a structured response
    result = {
        "resume_text": text[:500] + "..." if len(text) > 500 else text,  # Limit preview
        "entities": extracted_entities
    }

    return result
