import spacy
import fitz  # PyMuPDF for PDF text extraction

# Load spaCy model
nlp = spacy.load("en_core_web_sm")

def extract_text_from_pdf(pdf_file):
    """Extracts text from a PDF file."""
    try:
        doc = fitz.open("pdf", pdf_file.read())  # Correct way to load PDF from bytes
        text = "\n".join([page.get_text("text") for page in doc])
        return text.strip()
    except Exception as e:
        return f"Error extracting text: {str(e)}"

def analyze_resume(pdf_file):  # ✅ Expect a file object instead of path
    text = extract_text_from_pdf(pdf_file)  # ✅ Pass file object directly

    if not text or text.startswith("Error extracting text"):
        return {"error": "No text extracted from PDF"}

    # Process text with spaCy
    doc = nlp(text)

    # Extract named entities (skills, locations, etc.)
    extracted_entities = {ent.label_: [] for ent in doc.ents}
    for ent in doc.ents:
        extracted_entities[ent.label_].append(ent.text)

    # Create a structured response
    result = {
        "resume_text": text[:500] + ("..." if len(text) > 500 else ""),
        "entities": extracted_entities
    }

