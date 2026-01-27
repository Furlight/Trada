from .database import Base
from sqlalchemy import Column, Integer, String

# Exemple simple pour l'initialisation
class HealthCheck(Base):
    __tablename__ = "health_check"
    id = Column(Integer, primary_key=True, index=True)