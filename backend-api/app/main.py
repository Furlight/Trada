from fastapi import FastAPI, Depends, HTTPException, status
from fastapi.middleware.cors import CORSMiddleware
from fastapi.security import OAuth2PasswordBearer, OAuth2PasswordRequestForm
from sqlalchemy.orm import Session
from . import models, schemas, auth_utils
from .database import engine, Base, get_db

# Create database tables on startup
Base.metadata.create_all(bind=engine)

app = FastAPI(title="Trada API", version="1.0.0")

app.add_middleware(
    CORSMiddleware,
    # Allow local environment
    allow_origins=["http://localhost:8080"], 
    allow_credentials=True,
    allow_methods=["*"], # Allow POST, GET, PUT, etc.
    allow_headers=["*"], # Allow l'envoi de JSON
)

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

@app.post("/auth/register", response_model=schemas.UserResponse)
def register(user: schemas.UserCreate, db: Session = Depends(get_db)):

    db_user = db.query(models.User).filter(models.User.email == user.email).first()
    if db_user:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST, 
            detail="Email already registered"
        )
        
    db_pseudo = db.query(models.User).filter(models.User.pseudo == user.pseudo).first()
    if db_pseudo:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST, 
            detail="Username already taken"
        )

    hashed_pwd = auth_utils.get_password_hash(user.password)

    new_user = models.User(
        email=user.email,
        hashed_password=hashed_pwd, 
        pseudo=user.pseudo,
        first_name=user.first_name,
        last_name=user.last_name,
        phone_number=user.phone_number,
        country=user.country,
        wallet_address=user.wallet_address,
        is_active=True,
        is_verified=False
    )
    
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
    
    # Generate both tokens
    access_token = auth_utils.create_access_token(data={"sub": user.email})
    refresh_token = auth_utils.create_refresh_token(data={"sub": user.email})
    
    return {
        "access_token": access_token, 
        "refresh_token": refresh_token, 
        "token_type": "bearer"
    }

@app.post("/auth/refresh", response_model=schemas.Token)
def refresh_token(refresh_token: str, db: Session = Depends(get_db)):
    """
    Endpoint to obtain a new access_token using a valid refresh_token.
    """
    payload = auth_utils.decode_access_token(refresh_token)
    
    # Strict validation: check if it's a valid token AND specifically a refresh token
    if payload is None or payload.get("type") != "refresh":
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid or expired refresh token",
            headers={"WWW-Authenticate": "Bearer"},
        )
    
    email = payload.get("sub")
    user = db.query(models.User).filter(models.User.email == email).first()
    
    if not user:
        raise HTTPException(status_code=401, detail="User not found")
        
    # Issue a new access token
    new_access_token = auth_utils.create_access_token(data={"sub": user.email})
    
    # Optionally rotate the refresh token here too (security best practice), 
    # but for now, we just return the new access token and the same refresh token.
    return {
        "access_token": new_access_token, 
        "refresh_token": refresh_token, 
        "token_type": "bearer"
    }

@app.post("/auth/logout")
def logout(current_user: models.User = Depends(get_current_user)):
    """
    On a stateless JWT system, 'logout' primarily happens on the client 
    by deleting the token. This endpoint serves as a confirmation.
    """
    return {"message": "Successfully logged out. Please delete your token on the client side."}

# --- PROTECTED ROUTES ---

@app.get("/users/me", response_model=schemas.UserResponse)
def read_users_me(current_user: models.User = Depends(get_current_user)):
    """
    A hardened route that returns the authenticated user's profile.
    Only accessible with a valid JWT.
    """
    return current_user