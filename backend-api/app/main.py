from fastapi import FastAPI, Depends
from sqlalchemy.orm import Session
from sqlalchemy import text
from .database import engine, Base, get_db

Base.metadata.create_all(bind=engine)

app = FastAPI(title="Trada API")

@app.get("/")
def home():
    return {"message": "Trada Engine Online"}

@app.get("/health/db")
def test_db(db: Session = Depends(get_db)):
    try:
        # Modifie cette ligne :
        db.execute(text("SELECT 1")) 
        return {"status": "Database is reachable"}
    except Exception as e:
        return {"status": "Connection failed", "error": str(e)}