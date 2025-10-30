import logging
import time
import random
import hashlib
import numpy as np

logger = logging.getLogger(__name__)

class FaceDetectionService:
    def __init__(self):
        self.model_loaded = True  # Mock olarak her zaman True
        self.image_hashes = {}  # Store image hashes for consistent comparison
        logger.info("Mock Face Detection Service initialized")

    async def detect_faces(self, image_np):
        """Mock face detection - returns fake but valid results"""
        try:
            # Simulate processing time
            await self._simulate_processing()
            
            # Get image dimensions
            height, width = image_np.shape[:2] if len(image_np.shape) > 1 else (400, 300)
            
            # Create mock face detection result
            class MockFace:
                def __init__(self):
                    # Random but reasonable bounding box
                    margin = 50
                    self.bbox = [
                        margin, 
                        margin, 
                        width - margin, 
                        height - margin
                    ]
                    self.kps = None  # No keypoints for simplicity
                    self.det_score = round(random.uniform(0.85, 0.98), 3)  # High confidence
                    self.embedding = None  # No embedding for now
            
            # Return one mock face
            mock_faces = [MockFace()]
            
            logger.info(f"Mock detection: Found {len(mock_faces)} faces")
            return mock_faces
            
        except Exception as e:
            logger.error(f"Mock face detection error: {str(e)}")
            raise RuntimeError(f"Face detection failed: {str(e)}")
    
    def _calculate_image_hash(self, image):
        """Calculate a consistent hash for an image"""
        try:
            # Convert image to a consistent format for hashing
            if hasattr(image, 'tobytes'):
                image_bytes = image.tobytes()
            else:
                # If it's already bytes or another format
                image_bytes = str(image).encode()
            
            # Create MD5 hash
            hash_obj = hashlib.md5(image_bytes)
            return hash_obj.hexdigest()
        except Exception as e:
            logger.error(f"Error calculating image hash: {str(e)}")
            return "default_hash"
    
    def calculate_similarity(self, embedding1, embedding2):
        """Mock similarity calculation - returns consistent score based on input hash"""
        try:
            # For identical images/embeddings, return very high similarity
            if embedding1 is embedding2:
                logger.info("Mock similarity: identical objects detected - returning 0.999")
                return 0.999
            
            # Create a consistent hash from the embeddings/inputs
            hash_input1 = str(embedding1) if embedding1 is not None else "none1"
            hash_input2 = str(embedding2) if embedding2 is not None else "none2"
            
            # Sort the inputs to ensure same pair gives same result regardless of order
            inputs_sorted = sorted([hash_input1, hash_input2])
            combined_input = "".join(inputs_sorted)
            
            hash_obj = hashlib.md5(combined_input.encode())
            hash_hex = hash_obj.hexdigest()
            
            # Convert hash to a deterministic similarity score
            # For the same inputs, this will always return the same score
            hash_int = int(hash_hex[:8], 16)  # Use first 8 chars of hex
            
            # If inputs are very similar (like same image), boost similarity
            if hash_input1 == hash_input2:
                similarity = 0.95 + (hash_int % 50) / 1000  # Range: 0.95-0.999
                logger.info(f"Mock similarity: identical inputs detected - returning {similarity}")
            else:
                similarity = 0.3 + (hash_int % 600) / 1000  # Range: 0.3-0.9
                logger.info(f"Mock similarity: different inputs - returning {similarity}")
            
            similarity = round(similarity, 3)
            
            logger.info(f"Mock similarity calculated (deterministic): {similarity}")
            return similarity
        except Exception as e:
            logger.error(f"Mock similarity calculation error: {str(e)}")
            return 0.92  # High default similarity for mock testing
    
    async def _simulate_processing(self):
        """Simulate model processing time"""
        import asyncio
        processing_time = random.uniform(0.1, 0.3)  # 100-300ms
        await asyncio.sleep(processing_time)
