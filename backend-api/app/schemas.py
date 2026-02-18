from pydantic import BaseModel, EmailStr, Field, field_validator
import re
from typing import Optional

# ==============================================================================
# USER SCHEMAS
# ==============================================================================

class UserBase(BaseModel):
    """Fields common to all user operations."""
    email: EmailStr

class UserCreate(UserBase):
    """
    Schema for user registration with strict validation (Hardening).
    The 'email' field is automatically inherited from UserBase.
    """
    # 1. Strict constraints on lengths
    pseudo: str = Field(..., min_length=3, max_length=30, description="Username must be 3-30 characters")
    password: str = Field(..., min_length=8, description="Password must be at least 8 characters")
    
    # 2. KYC Fields (Optional, but constrained if provided)
    first_name: Optional[str] = Field(default="", max_length=50)
    last_name: Optional[str] = Field(default="", max_length=50)
    country: Optional[str] = Field(default="", max_length=50)
    
    # Phone number regex (Basic E.164 format check, e.g., +33606060606 or 0606060606)
    phone_number: Optional[str] = Field(default="", pattern=r"^\+?[0-9]{8,15}$")
    
    # 3. Web3 wallet address (Optional)
    wallet_address: Optional[str] = None
    
    is_active: bool = True
    is_superuser: bool = False
    is_verified: bool = False

    # 4. Advanced Password Validator (Requires 1 Uppercase & 1 Number)
    @field_validator('password')
    @classmethod
    def validate_password_strength(cls, v: str) -> str:
        if not re.search(r"[A-Z]", v):
            raise ValueError("Password must contain at least one uppercase letter")
        if not re.search(r"[0-9]", v):
            raise ValueError("Password must contain at least one digit")
        return v

class UserResponse(BaseModel):
    """
    Schema for returning user data.
    Explicitly filters out sensitive information (like passwords) 
    and allows KYC fields to pass through to the frontend.
    """
    id: int
    email: EmailStr
    pseudo: str
    
    # Status flags
    is_active: bool
    is_verified: bool

    # --- KYC Fields ---
    first_name: Optional[str] = None
    last_name: Optional[str] = None
    phone_number: Optional[str] = None
    country: Optional[str] = None
    
    # --- Web3 ---
    wallet_address: Optional[str] = None

    class Config:
        # Tells Pydantic to read the data even if it's an SQLAlchemy model
        from_attributes = True 
        # Note: If you use Pydantic V1 (older FastAPI), use this instead:
        # orm_mode = True

# ==============================================================================
# AUTH SCHEMAS
# ==============================================================================

class Token(BaseModel):
    """Response format for a successful login."""
    access_token: str
    refresh_token: str
    token_type: str

class TokenData(BaseModel):
    """Data contained inside the decoded JWT token."""
    email: Optional[str] = None