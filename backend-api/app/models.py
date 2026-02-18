from sqlalchemy import Column, Integer, String, Boolean
from .database import Base

class User(Base):
    __tablename__ = "users"
    # Creds
    id = Column(Integer, primary_key=True, index=True)
    email = Column(String, unique=True, index=True, nullable=False)
    hashed_password = Column(String, nullable=False)
    
    # Infos
    pseudo = Column(String, unique=True, index=True, nullable=False)
    first_name = Column(String, nullable=True, default="")
    last_name = Column(String, nullable=True, default="")
    phone_number = Column(String, nullable=True, default="")
    country = Column(String, nullable=True, default="")
    wallet_address = Column(String, unique=True, index=True, nullable=True)
    
    # Activation field
    is_active = Column(Boolean, default=True)
    is_verified = Column(Boolean, default=False)