from pydantic import BaseModel, EmailStr
from typing import Optional

# --- User Schemas ---

class UserBase(BaseModel):
    """Fields common to all user operations."""
    email: EmailStr

class UserCreate(UserBase):
    """Required data for sign-up."""
    password: str

class UserOut(UserBase):
    """Data returned to the client (without the password!)."""
    id: int
    is_active: bool

    class Config:
        from_attributes = True


# --- Auth Schemas ---

class Token(BaseModel):
    """Response format for a successful login."""
    access_token: str
    token_type: str

class TokenData(BaseModel):
    """Data contained inside the decoded JWT token."""
    email: Optional[str] = None