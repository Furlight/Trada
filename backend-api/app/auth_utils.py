import os
from datetime import datetime, timedelta
from typing import Optional
from jose import JWTError, ExpiredSignatureError, jwt
from passlib.context import CryptContext
from dotenv import load_dotenv

load_dotenv()

SECRET_KEY = os.getenv("SECRET_KEY")
ALGORITHM = os.getenv("ALGORITHM", "HS256")
REFRESH_TOKEN_EXPIRE_DAYS = int(os.getenv("REFRESH_TOKEN_EXPIRE_DAYS", 7))
ACCESS_TOKEN_EXPIRE_MINUTES = int(os.getenv("ACCESS_TOKEN_EXPIRE_MINUTES", 15))

# CryptContext handles hashing and verification using the bcrypt algorithm.
pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")

def get_password_hash(password: str) -> str:
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
    Generates a signed Access Token.
    """
    to_encode = data.copy()
    if expires_delta:
        expire = datetime.utcnow() + expires_delta
    else:
        expire = datetime.utcnow() + timedelta(minutes=ACCESS_TOKEN_EXPIRE_MINUTES)
    
    to_encode.update({"exp": expire})
    encoded_jwt = jwt.encode(to_encode, SECRET_KEY, algorithm=ALGORITHM)
    return encoded_jwt

def decode_access_token(token: str):
    """
    Decodes and validates a JWT. Returns the payload if valid, None otherwise.
    """
    try:
        payload = jwt.decode(token, SECRET_KEY, algorithms=[ALGORITHM])
        return payload
        
    except ExpiredSignatureError:
        # The token is authentic, but its lifespan has ended
        print("🚨 JWT ERROR: The token has expired!")
        return None
        
    except JWTError as e: 
        # The token is corrupted, malformed, or the SECRET_KEY doesn't match
        print(f"🚨 JWT ERROR: Invalid signature or format -> {e}")
        return None
        
    except Exception as e:
        # Catch-all for any other unexpected issues
        print(f"🚨 UNKNOWN ERROR -> {e}")
        return None
    
def create_refresh_token(data: dict) -> str:
    """
    Generates a long-lived Refresh Token.
    Adds a 'type': 'refresh' claim to distinguish it from access tokens.
    """
    to_encode = data.copy()
    expire = datetime.utcnow() + timedelta(days=REFRESH_TOKEN_EXPIRE_DAYS)
    to_encode.update({"exp": expire, "type": "refresh"})
    return jwt.encode(to_encode, SECRET_KEY, algorithm=ALGORITHM)