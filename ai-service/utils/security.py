"""
Security utilities for AI service
"""

import jwt
from typing import Dict, Any, Optional
import logging
from datetime import datetime, timedelta

logger = logging.getLogger(__name__)

class SecurityUtils:
    """Security utilities for JWT token validation"""
    
    @staticmethod
    def verify_jwt_token(token: str) -> Dict[str, Any]:
        """
        Verify JWT token and return payload
        
        Args:
            token: JWT token string
            
        Returns:
            Token payload dictionary
            
        Raises:
            jwt.InvalidTokenError: If token is invalid
        """
        try:
            # In a real implementation, you would:
            # 1. Get the secret key from environment or config
            # 2. Verify the token signature
            # 3. Check token expiration
            # 4. Validate token claims
            
            # For demonstration, we'll create a simple verification
            # In production, use proper JWT verification with secret key
            
            # Decode token without verification (for demo purposes)
            # In production, use jwt.decode with proper secret and algorithms
            payload = jwt.decode(
                token, 
                options={"verify_signature": False},  # Disable for demo
                algorithms=["HS256"]
            )
            
            # Check if token is expired
            if 'exp' in payload:
                exp_timestamp = payload['exp']
                if datetime.utcnow().timestamp() > exp_timestamp:
                    raise jwt.ExpiredSignatureError("Token has expired")
            
            # Validate required claims
            required_claims = ['sub', 'iat', 'exp']
            for claim in required_claims:
                if claim not in payload:
                    raise jwt.InvalidTokenError(f"Missing required claim: {claim}")
            
            logger.debug(f"JWT token verified for user: {payload.get('sub')}")
            return payload
            
        except jwt.ExpiredSignatureError:
            logger.warning("JWT token has expired")
            raise
        except jwt.InvalidTokenError as e:
            logger.warning(f"Invalid JWT token: {str(e)}")
            raise
        except Exception as e:
            logger.error(f"JWT token verification failed: {str(e)}")
            raise jwt.InvalidTokenError(f"Token verification failed: {str(e)}")
    
    @staticmethod
    def extract_user_info(payload: Dict[str, Any]) -> Dict[str, Any]:
        """
        Extract user information from JWT payload
        
        Args:
            payload: JWT token payload
            
        Returns:
            User information dictionary
        """
        try:
            user_info = {
                'user_id': payload.get('sub'),
                'username': payload.get('username'),
                'roles': payload.get('roles', []),
                'permissions': payload.get('permissions', []),
                'issued_at': payload.get('iat'),
                'expires_at': payload.get('exp'),
                'token_type': payload.get('type', 'access')
            }
            
            return user_info
            
        except Exception as e:
            logger.error(f"Failed to extract user info from payload: {str(e)}")
            return {}
    
    @staticmethod
    def check_permission(user_info: Dict[str, Any], required_permission: str) -> bool:
        """
        Check if user has required permission
        
        Args:
            user_info: User information dictionary
            required_permission: Required permission string
            
        Returns:
            True if user has permission, False otherwise
        """
        try:
            user_permissions = user_info.get('permissions', [])
            user_roles = user_info.get('roles', [])
            
            # Check direct permissions
            if required_permission in user_permissions:
                return True
            
            # Check role-based permissions
            role_permissions = {
                'ADMIN': ['ANALYSIS_PERFORM', 'REPORT_GENERATE', 'USER_MANAGE'],
                'INVESTIGATOR': ['ANALYSIS_PERFORM', 'REPORT_GENERATE'],
                'ANALYST': ['ANALYSIS_PERFORM', 'REPORT_GENERATE'],
                'VIEWER': ['REPORT_READ'],
                'AUDITOR': ['AUDIT_READ', 'REPORT_READ']
            }
            
            for role in user_roles:
                if role in role_permissions:
                    if required_permission in role_permissions[role]:
                        return True
            
            return False
            
        except Exception as e:
            logger.error(f"Failed to check permission: {str(e)}")
            return False
    
    @staticmethod
    def validate_request_origin(request_headers: Dict[str, str]) -> bool:
        """
        Validate request origin for security
        
        Args:
            request_headers: Request headers dictionary
            
        Returns:
            True if origin is valid, False otherwise
        """
        try:
            # Check for required headers
            required_headers = ['User-Agent', 'X-Requested-With']
            for header in required_headers:
                if header not in request_headers:
                    logger.warning(f"Missing required header: {header}")
                    return False
            
            # Check User-Agent (basic validation)
            user_agent = request_headers.get('User-Agent', '')
            if len(user_agent) < 10:  # Basic length check
                logger.warning("Suspicious User-Agent")
                return False
            
            # Check for suspicious patterns
            suspicious_patterns = ['curl', 'wget', 'python-requests']
            if any(pattern in user_agent.lower() for pattern in suspicious_patterns):
                logger.warning(f"Suspicious User-Agent: {user_agent}")
                # In production, you might want to block these
                # For demo purposes, we'll allow them
            
            return True
            
        except Exception as e:
            logger.error(f"Failed to validate request origin: {str(e)}")
            return False
    
    @staticmethod
    def sanitize_input(input_data: Any) -> Any:
        """
        Sanitize input data to prevent injection attacks
        
        Args:
            input_data: Input data to sanitize
            
        Returns:
            Sanitized input data
        """
        try:
            if isinstance(input_data, str):
                # Remove potentially dangerous characters
                dangerous_chars = ['<', '>', '"', "'", '&', ';', '|', '`', '$']
                for char in dangerous_chars:
                    input_data = input_data.replace(char, '')
                
                # Limit length
                if len(input_data) > 1000:
                    input_data = input_data[:1000]
                
                return input_data
            
            elif isinstance(input_data, dict):
                # Recursively sanitize dictionary values
                sanitized = {}
                for key, value in input_data.items():
                    sanitized[key] = SecurityUtils.sanitize_input(value)
                return sanitized
            
            elif isinstance(input_data, list):
                # Recursively sanitize list items
                return [SecurityUtils.sanitize_input(item) for item in input_data]
            
            else:
                # Return as-is for other types
                return input_data
                
        except Exception as e:
            logger.error(f"Failed to sanitize input: {str(e)}")
            return input_data
    
    @staticmethod
    def generate_secure_filename(original_filename: str) -> str:
        """
        Generate a secure filename
        
        Args:
            original_filename: Original filename
            
        Returns:
            Secure filename
        """
        try:
            import uuid
            import os
            
            # Get file extension
            _, ext = os.path.splitext(original_filename)
            
            # Generate secure filename
            secure_filename = f"{uuid.uuid4().hex}{ext}"
            
            return secure_filename
            
        except Exception as e:
            logger.error(f"Failed to generate secure filename: {str(e)}")
            return f"secure_file_{uuid.uuid4().hex}"
    
    @staticmethod
    def validate_file_type(filename: str, allowed_extensions: list) -> bool:
        """
        Validate file type based on extension
        
        Args:
            filename: Filename to validate
            allowed_extensions: List of allowed extensions
            
        Returns:
            True if file type is allowed, False otherwise
        """
        try:
            import os
            
            _, ext = os.path.splitext(filename.lower())
            
            return ext in allowed_extensions
            
        except Exception as e:
            logger.error(f"Failed to validate file type: {str(e)}")
            return False
    
    @staticmethod
    def calculate_file_hash(file_content: bytes) -> str:
        """
        Calculate SHA-256 hash of file content
        
        Args:
            file_content: File content as bytes
            
        Returns:
            SHA-256 hash as hex string
        """
        try:
            import hashlib
            
            hash_object = hashlib.sha256(file_content)
            return hash_object.hexdigest()
            
        except Exception as e:
            logger.error(f"Failed to calculate file hash: {str(e)}")
            return ""

# Convenience function for JWT verification
def verify_jwt_token(token: str) -> Dict[str, Any]:
    """Convenience function for JWT token verification"""
    return SecurityUtils.verify_jwt_token(token)
