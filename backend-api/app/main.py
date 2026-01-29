from fastapi import FastAPI, Depends, HTTPException, status
from fastapi.security import OAuth2PasswordBearer, OAuth2PasswordRequestForm
from sqlalchemy.orm import Session
from . import models, schemas, auth_utils
from .database import engine, Base, get_db

# Create database tables on startup
Base.metadata.create_all(bind=engine)

app = FastAPI(title="Trada API", version="1.0.0")

# Define the OAuth2 scheme: tells FastAPI where to get the token (the login route)
oauth2_scheme = OAuth2PasswordBearer(tokenUrl="auth/login")

# --- DEPENDENCIES ---

def get_current_user(db: Session = Depends(get_db), token: str = Depends(oauth2_scheme)):
    """
    Hardened dependency to protect routes.
    Decodes the JWT and retrieves the user from the database.
    """
    credentials_exception = HTTPException(
        status_code=status.HTTP_401_UNAUTHORIZED,
        detail="Could not validate credentials",
        headers={"WWW-Authenticate": "Bearer"},
    )
    
    # Use our hardened decode utility
    payload = auth_utils.decode_access_token(token)
    if payload is None:
        raise credentials_exception
        
    email: str = payload.get("sub")
    if email is None:
        raise credentials_exception
        
    # Check if the user still exists in DB
    user = db.query(models.User).filter(models.User.email == email).first()
    if user is None:
        raise credentials_exception
        
    return user

# --- PUBLIC ROUTES ---

@app.get("/")
def root():
    return {"message": "Trada API is online"}

@app.post("/auth/register", response_model=schemas.UserOut, status_code=status.HTTP_201_CREATED)
def register(user: schemas.UserCreate, db: Session = Depends(get_db)):
    db_user = db.query(models.User).filter(models.User.email == user.email).first()
    if db_user:
        raise HTTPException(status_code=400, detail="Email already registered")
    
    hashed_pwd = auth_utils.hash_password(user.password)
    new_user = models.User(email=user.email, hashed_password=hashed_pwd)
    
    db.add(new_user)
    db.commit()
    db.refresh(new_user)
    return new_user

@app.post("/auth/login", response_model=schemas.Token)
def login(form_data: OAuth2PasswordRequestForm = Depends(), db: Session = Depends(get_db)):
    user = db.query(models.User).filter(models.User.email == form_data.username).first()
    
    if not user or not auth_utils.verify_password(form_data.password, user.hashed_password):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED, 
            detail="Incorrect email or password",
            headers={"WWW-Authenticate": "Bearer"},
        )
    
    access_token = auth_utils.create_access_token(data={"sub": user.email})
    return {"access_token": access_token, "token_type": "bearer"}

@app.post("/auth/logout")
def logout(current_user: models.User = Depends(get_current_user)):
    """
    On a stateless JWT system, 'logout' primarily happens on the client 
    by deleting the token. This endpoint serves as a confirmation.
    """
    return {"message": "Successfully logged out. Please delete your token on the client side."}

# --- PROTECTED ROUTES ---

@app.get("/users/me", response_model=schemas.UserOut)
def read_users_me(current_user: models.User = Depends(get_current_user)):
    """
    A hardened route that returns the authenticated user's profile.
    Only accessible with a valid JWT.
    """
    return current_user