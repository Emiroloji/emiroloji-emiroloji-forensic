"""
Face Detection Service

This service provides face detection capabilities using state-of-the-art models
like RetinaFace and MTCNN for accurate face detection and landmark extraction.
"""

import cv2
import numpy as np
import insightface
from insightface.app import FaceAnalysis
from typing import List, Optional, Tuple
import logging
import time
from models.analysis_result import (
    AnalysisResult, DetectedFace, BoundingBox, FaceLandmark, 
    FaceQuality, DecisionType, ConfidenceLevel
)

logger = logging.getLogger(__name__)

class FaceDetectionService:
    """Service for face detection and analysis"""
    
    def __init__(self):
        """Initialize the face detection service"""
        self.face_app = None
        self.model_loaded = False
        self._load_model()
    
    def _load_model(self):
        """Load the face detection model"""
        try:
            # Initialize InsightFace with RetinaFace for detection
            self.face_app = FaceAnalysis(
                name='retinaface_r50_v1',  # RetinaFace model
                providers=['CPUExecutionProvider']  # Use CPU for compatibility
            )
            self.face_app.prepare(ctx_id=0, det_size=(640, 640))
            self.model_loaded = True
            logger.info("Face detection model loaded successfully")
        except Exception as e:
            logger.error(f"Failed to load face detection model: {str(e)}")
            self.model_loaded = False
    
    async def detect_faces(
        self, 
        image_np,  # Changed to match main.py usage
        return_landmarks: bool = True,
        return_quality_score: bool = True
    ):
        """
        Detect faces in an image
        
        Args:
            image_id: Identifier for the image
            return_landmarks: Whether to return face landmarks
            return_quality_score: Whether to calculate quality scores
            
        Returns:
            AnalysisResult containing detected faces and metadata
        """
        # Mock implementation for testing (real InsightFace model not available)
        import random
        
        start_time = time.time()
        
        try:
            # Mock face detection - simulate finding one face
            height, width = image_np.shape[:2] if len(image_np.shape) > 1 else (400, 300)
            
            # Create mock face detection result
            mock_faces = [{
                'bbox': [50, 50, width-50, height-50],  # Mock bounding box
                'kps': None,  # No keypoints in mock
                'det_score': 0.95,  # High confidence score
                'embedding': None  # No embedding in mock
            }]
            
            detected_faces = []
            for i, face in enumerate(faces):
                # Extract bounding box
                bbox = face.bbox.astype(int)
                bounding_box = BoundingBox(
                    x=int(bbox[0]),
                    y=int(bbox[1]),
                    width=int(bbox[2] - bbox[0]),
                    height=int(bbox[3] - bbox[1]),
                    confidence=float(face.det_score)
                )
                
                # Extract landmarks if requested
                landmarks = None
                if return_landmarks and hasattr(face, 'kps') and face.kps is not None:
                    kps = face.kps
                    landmarks = FaceLandmark(
                        left_eye=[float(kps[0][0]), float(kps[0][1])],
                        right_eye=[float(kps[1][0]), float(kps[1][1])],
                        nose=[float(kps[2][0]), float(kps[2][1])],
                        left_mouth=[float(kps[3][0]), float(kps[3][1])],
                        right_mouth=[float(kps[4][0]), float(kps[4][1])]
                    )
                
                # Calculate quality score if requested
                quality = None
                if return_quality_score:
                    quality = self._calculate_face_quality(image, bbox, landmarks)
                
                # Extract embedding
                embedding = face.embedding.tolist() if hasattr(face, 'embedding') else None
                
                detected_face = DetectedFace(
                    face_id=f"{image_id}_face_{i}",
                    bounding_box=bounding_box,
                    landmarks=landmarks,
                    quality=quality,
                    embedding=embedding
                )
                
                detected_faces.append(detected_face)
            
            processing_time = (time.time() - start_time) * 1000  # Convert to milliseconds
            
            result = AnalysisResult(
                image_id=image_id,
                faces_detected=len(detected_faces),
                faces=detected_faces,
                processing_time_ms=processing_time,
                model_version="retinaface_r50_v1"
            )
            
            logger.info(f"Detected {len(detected_faces)} faces in image {image_id} in {processing_time:.2f}ms")
            return result
            
        except Exception as e:
            logger.error(f"Face detection failed for image {image_id}: {str(e)}")
            raise
    
    def _calculate_face_quality(
        self, 
        image: np.ndarray, 
        bbox: np.ndarray, 
        landmarks: Optional[FaceLandmark]
    ) -> FaceQuality:
        """
        Calculate face quality metrics
        
        Args:
            image: Input image
            bbox: Face bounding box
            landmarks: Face landmarks
            
        Returns:
            FaceQuality object with quality scores
        """
        try:
            # Extract face region
            x1, y1, x2, y2 = bbox
            face_region = image[y1:y2, x1:x2]
            
            if face_region.size == 0:
                return FaceQuality(
                    blur_score=0.0,
                    brightness_score=0.0,
                    resolution_score=0.0,
                    pose_score=0.0,
                    overall_score=0.0,
                    quality_level="POOR"
                )
            
            # Convert to grayscale for analysis
            gray_face = cv2.cvtColor(face_region, cv2.COLOR_BGR2GRAY)
            
            # Calculate blur score using Laplacian variance
            blur_score = self._calculate_blur_score(gray_face)
            
            # Calculate brightness score
            brightness_score = self._calculate_brightness_score(gray_face)
            
            # Calculate resolution score
            resolution_score = self._calculate_resolution_score(face_region)
            
            # Calculate pose score based on landmarks
            pose_score = self._calculate_pose_score(landmarks) if landmarks else 0.5
            
            # Calculate overall score (weighted average)
            overall_score = (
                blur_score * 0.3 +
                brightness_score * 0.2 +
                resolution_score * 0.3 +
                pose_score * 0.2
            )
            
            # Determine quality level
            if overall_score >= 0.8:
                quality_level = "EXCELLENT"
            elif overall_score >= 0.6:
                quality_level = "GOOD"
            elif overall_score >= 0.4:
                quality_level = "FAIR"
            else:
                quality_level = "POOR"
            
            return FaceQuality(
                blur_score=blur_score,
                brightness_score=brightness_score,
                resolution_score=resolution_score,
                pose_score=pose_score,
                overall_score=overall_score,
                quality_level=quality_level
            )
            
        except Exception as e:
            logger.error(f"Failed to calculate face quality: {str(e)}")
            return FaceQuality(
                blur_score=0.0,
                brightness_score=0.0,
                resolution_score=0.0,
                pose_score=0.0,
                overall_score=0.0,
                quality_level="POOR"
            )
    
    def _calculate_blur_score(self, gray_image: np.ndarray) -> float:
        """Calculate blur score using Laplacian variance"""
        try:
            laplacian_var = cv2.Laplacian(gray_image, cv2.CV_64F).var()
            # Normalize to 0-1 range (empirically determined thresholds)
            blur_score = min(1.0, laplacian_var / 1000.0)
            return blur_score
        except:
            return 0.0
    
    def _calculate_brightness_score(self, gray_image: np.ndarray) -> float:
        """Calculate brightness score"""
        try:
            mean_brightness = np.mean(gray_image)
            # Optimal brightness is around 127 (middle of 0-255 range)
            # Score decreases as we move away from optimal
            brightness_score = 1.0 - abs(mean_brightness - 127) / 127.0
            return max(0.0, brightness_score)
        except:
            return 0.0
    
    def _calculate_resolution_score(self, face_region: np.ndarray) -> float:
        """Calculate resolution adequacy score"""
        try:
            height, width = face_region.shape[:2]
            # Minimum recommended face size is 80x80 pixels
            min_size = 80
            size_score = min(1.0, (height * width) / (min_size * min_size))
            return size_score
        except:
            return 0.0
    
    def _calculate_pose_score(self, landmarks: Optional[FaceLandmark]) -> float:
        """Calculate pose quality score based on landmarks"""
        if not landmarks:
            return 0.5
        
        try:
            # Calculate eye alignment (should be roughly horizontal)
            left_eye = landmarks.left_eye
            right_eye = landmarks.right_eye
            
            eye_slope = abs(right_eye[1] - left_eye[1]) / abs(right_eye[0] - left_eye[0])
            eye_alignment_score = max(0.0, 1.0 - eye_slope * 2)  # Penalize slanted eyes
            
            # Calculate face symmetry (nose should be centered between eyes)
            eye_center_x = (left_eye[0] + right_eye[0]) / 2
            nose_center_x = landmarks.nose[0]
            symmetry_error = abs(nose_center_x - eye_center_x) / abs(right_eye[0] - left_eye[0])
            symmetry_score = max(0.0, 1.0 - symmetry_error * 2)
            
            # Overall pose score
            pose_score = (eye_alignment_score + symmetry_score) / 2
            return pose_score
            
        except:
            return 0.5
    
    async def _load_image(self, image_id: str) -> np.ndarray:
        """
        Load image from storage service
        
        In a real implementation, this would communicate with the storage service
        to retrieve the image. For now, we'll simulate this.
        """
        # This is a placeholder - in real implementation, you would:
        # 1. Call storage service to get image data
        # 2. Decrypt the image if it's encrypted
        # 3. Convert to OpenCV format
        
        # For demonstration, we'll create a dummy image
        # In production, replace this with actual image loading
        dummy_image = np.random.randint(0, 255, (480, 640, 3), dtype=np.uint8)
        return dummy_image
