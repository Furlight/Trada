import os
from datetime import datetime, timedelta
from typing import Optional
from jose import JWTError, jwt
from passlib.context import CryptContext
from dotenv import load_dotenv

load_dotenv()

# --- Password Hashing Configuration ---
# CryptContext handles hashing and verification using the bcrypt algorithm.
pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")

# --- JWT Configuration ---
# Secret key is pulled from .env for security. Algorithm and expiration have safe defaults.
SECRET_KEY = os.getenv("SECRET_KEY")
ALGORITHM = os.getenv("ALGORITHM", "HS256")
ACCESS_TOKEN_EXPIRE_MINUTES = int(os.getenv("ACCESS_TOKEN_EXPIRE_MINUTES", 30))

def hash_password(password: str) -> str:
    """
    Hashes a plain-text password using bcrypt.
    Note: bcrypt has a limit of 72 characters.
    """
    return pwd_context.hash(password)

def verify_password(plain_password: str, hashed_password: str) -> bool:
    """
    Compares a plain-text password with its hashed version to verify validity.
    """
    return pwd_context.verify(plain_password, hashed_password)

def create_access_token(data: dict, expires_delta: Optional[timedelta] = None) -> str:
    """
    Generates a signed JWT (JSON Web Token).
    - data: Dict containing the claims (e.g., {"sub": user_email}).
    - expires_delta: Optional override for token lifespan.
    """
    to_encode = data.copy()
    if expires_delta:
        expire = datetime.utcnow() + expires_delta
    else:
        expire = datetime.utcnow() + timedelta(minutes=ACCESS_TOKEN_EXPIRE_MINUTES)
    
    to_encode.update({"exp": expire})
    encoded_jwt = jwt.encode(to_encode, SECRET_KEY, algorithm=ALGORITHM)
    return encoded_jwt

def decode_access_token(token: str) -> Optional[dict]:
    """
    Validates and decodes a JWT.
    Returns the payload if valid, or None if the token is expired/invalid.
    This effectively uses the JWTError import to catch tampering or expiration.
    """
    try:
        payload = jwt.decode(token, SECRET_KEY, algorithms=[ALGORITHM])
        return payload
    except JWTError:
        # JWTError handles signature verification, expiration, and formatting errors.
        return None