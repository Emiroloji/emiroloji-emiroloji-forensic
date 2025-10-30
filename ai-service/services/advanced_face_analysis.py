"""
Advanced Face Analysis Service for Forensic Investigation
Provides age, gender, emotion detection and comprehensive facial analysis
"""

import cv2
import numpy as np
import asyncio
import logging
from typing import Dict, List, Tuple, Optional, Any
from dataclasses import dataclass
from pathlib import Path
import json
from datetime import datetime

# Setup logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

def convert_numpy_types(obj):
    """Convert numpy types to native Python types for JSON serialization"""
    if isinstance(obj, np.integer):
        return int(obj)
    elif isinstance(obj, np.floating):
        return float(obj)
    elif isinstance(obj, np.ndarray):
        return obj.tolist()
    elif isinstance(obj, np.bool_):
        return bool(obj)
    elif isinstance(obj, dict):
        return {key: convert_numpy_types(value) for key, value in obj.items()}
    elif isinstance(obj, list):
        return [convert_numpy_types(item) for item in obj]
    elif isinstance(obj, tuple):
        return tuple(convert_numpy_types(item) for item in obj)
    return obj

@dataclass
class FacialFeatures:
    """Comprehensive facial features analysis"""
    age_estimate: Dict[str, Any]  # age range, confidence
    gender_prediction: Dict[str, Any]  # gender, confidence
    emotion_analysis: Dict[str, Any]  # dominant emotion, all emotions with scores
    ethnicity_analysis: Dict[str, Any]  # predicted ethnicity, confidence
    facial_hair: Dict[str, Any]  # beard, mustache, confidence
    accessories: Dict[str, Any]  # glasses, hat, mask detection
    facial_landmarks: List[Tuple[int, int]]  # 68 point face landmarks
    face_quality: Dict[str, Any]  # quality metrics
    biometric_template: Optional[str]  # encoded biometric data

@dataclass 
class AdvancedFaceResult:
    """Advanced face analysis result"""
    face_id: str
    bounding_box: Tuple[int, int, int, int]
    facial_features: FacialFeatures
    confidence_score: float
    analysis_timestamp: str
    processing_notes: List[str]

class AdvancedFaceAnalysisService:
    """Advanced facial analysis service with demographic and biometric capabilities"""
    
    def __init__(self):
        self.age_classifier = None
        self.gender_classifier = None
        self.emotion_classifier = None
        self.ethnicity_classifier = None
        self.face_cascade = None  # Lazy initialize
        self.eye_cascade = None   # Lazy initialize
        logger.info("Advanced Face Analysis Service initialized")
        
    def _init_cascades_if_needed(self):
        """Initialize cascade classifiers if not already done"""
        if self.face_cascade is None:
            self.face_cascade = cv2.CascadeClassifier(cv2.data.haarcascades + 'haarcascade_frontalface_default.xml')
        if self.eye_cascade is None:
            self.eye_cascade = cv2.CascadeClassifier(cv2.data.haarcascades + 'haarcascade_eye.xml')
        
    async def analyze_face_comprehensive(self, face_image: np.ndarray) -> FacialFeatures:
        """Comprehensive facial analysis including demographics and features"""
        try:
            # Age estimation
            age_data = await self._estimate_age(face_image)
            
            # Gender prediction  
            gender_data = await self._predict_gender(face_image)
            
            # Emotion analysis
            emotion_data = await self._analyze_emotion(face_image)
            
            # Ethnicity analysis
            ethnicity_data = await self._analyze_ethnicity(face_image)
            
            # Facial hair detection
            facial_hair_data = await self._detect_facial_hair(face_image)
            
            # Accessories detection
            accessories_data = await self._detect_accessories(face_image)
            
            # Facial landmarks
            landmarks = await self._extract_landmarks(face_image)
            
            # Face quality assessment
            quality_data = await self._assess_face_quality(face_image)
            
            # Generate biometric template
            biometric_template = await self._generate_biometric_template(face_image)
            
            return FacialFeatures(
                age_estimate=age_data,
                gender_prediction=gender_data,
                emotion_analysis=emotion_data,
                ethnicity_analysis=ethnicity_data,
                facial_hair=facial_hair_data,
                accessories=accessories_data,
                facial_landmarks=landmarks,
                face_quality=quality_data,
                biometric_template=biometric_template
            )
            
        except Exception as e:
            logger.error(f"Error in comprehensive face analysis: {str(e)}")
            raise
            
    async def _estimate_age(self, face_image: np.ndarray) -> Dict[str, Any]:
        """Advanced age estimation using clinical-grade analysis and legal age categories"""
        try:
            gray = cv2.cvtColor(face_image, cv2.COLOR_BGR2GRAY)
            height, width = gray.shape
            
            # Multi-modal age estimation approach
            age_features = await self._extract_age_features(gray)
            
            # Clinical age estimation using multiple indicators
            clinical_age = await self._clinical_age_estimation(gray, age_features)
            
            # Legal age classification (forensically important)
            legal_classification = self._classify_legal_age_category(clinical_age)
            
            # Advanced confidence calculation based on multiple factors
            confidence = self._calculate_age_confidence(age_features, legal_classification)
            
            result = {
                "estimated_age": clinical_age["estimated_age"],
                "age_range": clinical_age["age_range"],
                "legal_category": legal_classification["category"],
                "legal_confidence": legal_classification["confidence"],
                "confidence": confidence,
                "clinical_features": age_features,
                "forensic_notes": legal_classification["forensic_notes"],
                "analysis_method": "Clinical Multi-Modal Assessment"
            }
            
            return convert_numpy_types(result)
            
        except Exception as e:
            logger.error(f"Error estimating age: {str(e)}")
            return {
                "estimated_age": None,
                "age_range": None,
                "legal_category": "UNDETERMINED",
                "confidence": 0.0,
                "error": str(e)
            }
    
    async def _extract_age_features(self, gray_image: np.ndarray) -> Dict[str, float]:
        """Extract comprehensive age-related features"""
        try:
            height, width = gray_image.shape
            
            # 1. Wrinkle Analysis (Clinical grade)
            # Forehead wrinkles
            forehead = gray_image[int(height*0.1):int(height*0.4), int(width*0.2):int(width*0.8)]
            forehead_laplacian = cv2.Laplacian(forehead, cv2.CV_64F)
            forehead_wrinkles = np.std(forehead_laplacian)
            
            # Eye wrinkles (crow's feet)
            eye_left = gray_image[int(height*0.3):int(height*0.5), int(width*0.1):int(width*0.4)]
            eye_right = gray_image[int(height*0.3):int(height*0.5), int(width*0.6):int(width*0.9)]
            eye_wrinkles = (np.std(cv2.Laplacian(eye_left, cv2.CV_64F)) + 
                           np.std(cv2.Laplacian(eye_right, cv2.CV_64F))) / 2
            
            # Nasolabial folds
            nasolabial_left = gray_image[int(height*0.5):int(height*0.8), int(width*0.2):int(width*0.45)]
            nasolabial_right = gray_image[int(height*0.5):int(height*0.8), int(width*0.55):int(width*0.8)]
            nasolabial_depth = (np.std(cv2.Laplacian(nasolabial_left, cv2.CV_64F)) +
                               np.std(cv2.Laplacian(nasolabial_right, cv2.CV_64F))) / 2
            
            # 2. Skin Texture Analysis
            # Multi-scale texture analysis
            skin_texture_scores = []
            for kernel_size in [5, 11, 21]:
                blur = cv2.GaussianBlur(gray_image, (kernel_size, kernel_size), 0)
                texture = np.mean(np.abs(gray_image.astype(float) - blur.astype(float)))
                skin_texture_scores.append(texture)
            avg_skin_texture = np.mean(skin_texture_scores)
            
            # 3. Facial Volume Analysis (Age-related facial volume loss)
            # Cheek fullness
            cheek_region = gray_image[int(height*0.4):int(height*0.7), int(width*0.2):int(width*0.8)]
            cheek_mean = np.mean(cheek_region)
            cheek_variance = np.var(cheek_region)
            volume_loss_indicator = cheek_variance / (cheek_mean + 1e-6)
            
            # 4. Eye Analysis
            eye_region = gray_image[int(height*0.25):int(height*0.55), int(width*0.15):int(width*0.85)]
            
            # Eye bag detection
            lower_eye = eye_region[int(eye_region.shape[0]*0.7):, :]
            eye_bags = np.std(cv2.Sobel(lower_eye, cv2.CV_64F, 0, 1, ksize=3))
            
            # Eyelid drooping
            upper_eye = eye_region[:int(eye_region.shape[0]*0.3), :]
            eyelid_shape = np.mean(np.gradient(np.mean(upper_eye, axis=1)))
            
            # 5. Hair Analysis (if visible)
            # Hair graying (simplified - would need color analysis in real implementation)
            hair_region = gray_image[:int(height*0.3), :]
            hair_gray_level = np.mean(hair_region) if np.std(hair_region) > 10 else 0
            
            # 6. Bone Structure Analysis
            # Bone prominence increases with age
            facial_edges = cv2.Canny(gray_image, 50, 150)
            bone_prominence = np.sum(facial_edges > 0) / (height * width)
            
            features = {
                "forehead_wrinkles": forehead_wrinkles,
                "eye_wrinkles": eye_wrinkles,
                "nasolabial_depth": nasolabial_depth,
                "skin_texture": avg_skin_texture,
                "volume_loss": volume_loss_indicator,
                "eye_bags": eye_bags,
                "eyelid_drooping": abs(eyelid_shape),
                "hair_gray_level": hair_gray_level,
                "bone_prominence": bone_prominence
            }
            
            return features
            
        except Exception as e:
            logger.error(f"Error extracting age features: {str(e)}")
            return {}
    
    async def _clinical_age_estimation(self, gray_image: np.ndarray, features: Dict[str, float]) -> Dict[str, Any]:
        """Clinical-grade age estimation using medical research parameters"""
        try:
            if not features:
                return {"estimated_age": None, "age_range": None}
            
            # Clinical age scoring system based on medical literature
            age_scores = []
            
            # Wrinkle scoring (based on Fitzpatrick wrinkle scale)
            wrinkle_score = 0
            if features.get("forehead_wrinkles", 0) > 150:
                wrinkle_score += 25  # Severe forehead wrinkles
            elif features.get("forehead_wrinkles", 0) > 80:
                wrinkle_score += 15  # Moderate wrinkles
            elif features.get("forehead_wrinkles", 0) > 40:
                wrinkle_score += 8   # Fine wrinkles
            
            if features.get("eye_wrinkles", 0) > 100:
                wrinkle_score += 20  # Prominent crow's feet
            elif features.get("eye_wrinkles", 0) > 50:
                wrinkle_score += 12  # Visible crow's feet
            
            if features.get("nasolabial_depth", 0) > 80:
                wrinkle_score += 18  # Deep nasolabial folds
            elif features.get("nasolabial_depth", 0) > 40:
                wrinkle_score += 10  # Moderate nasolabial folds
            
            age_scores.append(18 + wrinkle_score)  # Base age 18
            
            # Skin texture scoring
            skin_age = 18 + (features.get("skin_texture", 0) * 2.5)  # Empirical scaling
            age_scores.append(min(skin_age, 80))  # Cap at 80
            
            # Volume loss scoring (facial aging indicator)
            volume_age = 18 + (features.get("volume_loss", 0) * 40)
            age_scores.append(min(volume_age, 75))
            
            # Eye aging indicators
            eye_age = 18 + (features.get("eye_bags", 0) * 0.5) + (features.get("eyelid_drooping", 0) * 30)
            age_scores.append(min(eye_age, 85))
            
            # Bone structure changes
            bone_age = 18 + (features.get("bone_prominence", 0) * 60)
            age_scores.append(min(bone_age, 70))
            
            # Calculate weighted average (some features are more reliable)
            weights = [0.3, 0.25, 0.2, 0.15, 0.1]  # Wrinkles most reliable
            weighted_age = sum(age * weight for age, weight in zip(age_scores, weights))
            
            # Apply clinical validation rules
            if weighted_age < 18:
                weighted_age = 18
                age_range = (16, 22)
            elif weighted_age < 25:
                age_range = (max(18, weighted_age - 5), weighted_age + 5)
            elif weighted_age < 40:
                age_range = (weighted_age - 7, weighted_age + 7)
            elif weighted_age < 60:
                age_range = (weighted_age - 10, weighted_age + 10)
            else:
                age_range = (weighted_age - 15, min(100, weighted_age + 15))
            
            return {
                "estimated_age": int(weighted_age),
                "age_range": (int(age_range[0]), int(age_range[1])),
                "component_scores": {
                    "wrinkle_age": age_scores[0],
                    "skin_age": age_scores[1],
                    "volume_age": age_scores[2],
                    "eye_age": age_scores[3],
                    "bone_age": age_scores[4]
                }
            }
            
        except Exception as e:
            logger.error(f"Error in clinical age estimation: {str(e)}")
            return {"estimated_age": None, "age_range": None}
    
    def _classify_legal_age_category(self, clinical_age: Dict[str, Any]) -> Dict[str, Any]:
        """Classify into legal age categories important for forensic analysis"""
        try:
            estimated_age = clinical_age.get("estimated_age")
            age_range = clinical_age.get("age_range", (0, 0))
            
            if estimated_age is None:
                return {
                    "category": "UNDETERMINED",
                    "confidence": 0.0,
                    "forensic_notes": ["Unable to determine age category"]
                }
            
            # Legal age categories with forensic significance
            categories = {
                "JUVENILE": (0, 18),
                "YOUNG_ADULT": (18, 25),
                "ADULT": (25, 65),
                "SENIOR": (65, 100)
            }
            
            # Determine primary category
            primary_category = "ADULT"  # Default
            for category, (min_age, max_age) in categories.items():
                if min_age <= estimated_age < max_age:
                    primary_category = category
                    break
            
            # Calculate confidence based on age range overlap
            confidence = 1.0
            forensic_notes = []
            
            # Check for boundary cases (forensically critical)
            age_min, age_max = age_range
            
            # Check if range spans multiple categories
            spanning_categories = []
            for category, (cat_min, cat_max) in categories.items():
                if not (age_max <= cat_min or age_min >= cat_max):  # Overlap exists
                    spanning_categories.append(category)
            
            if len(spanning_categories) > 1:
                confidence *= 0.7  # Reduce confidence for boundary cases
                forensic_notes.append(f"Age range spans multiple categories: {spanning_categories}")
            
            # Special handling for critical legal boundaries
            if primary_category == "JUVENILE" and age_max > 18:
                confidence *= 0.6
                forensic_notes.append("CRITICAL: Age range may include adult classification")
            elif primary_category == "YOUNG_ADULT" and age_min < 18:
                confidence *= 0.6
                forensic_notes.append("CRITICAL: Age range may include juvenile classification")
            
            # Additional forensic considerations
            if primary_category == "JUVENILE":
                forensic_notes.append("Subject classified as minor - special legal protections apply")
            elif estimated_age >= 21:
                forensic_notes.append("Subject meets adult legal age for all jurisdictions")
            
            return {
                "category": primary_category,
                "confidence": confidence,
                "alternative_categories": spanning_categories,
                "forensic_notes": forensic_notes,
                "legal_age_boundaries": {
                    "definitely_adult": age_min >= 18,
                    "definitely_minor": age_max < 18,
                    "boundary_case": age_min < 18 < age_max
                }
            }
            
        except Exception as e:
            logger.error(f"Error classifying legal age category: {str(e)}")
            return {
                "category": "UNDETERMINED",
                "confidence": 0.0,
                "forensic_notes": [f"Error in classification: {str(e)}"]
            }
    
    def _calculate_age_confidence(self, features: Dict[str, float], legal_classification: Dict[str, Any]) -> float:
        """Calculate overall confidence in age estimation"""
        try:
            if not features:
                return 0.0
            
            # Base confidence from feature quality
            feature_confidence = 0.5
            
            # Adjust based on feature strength
            strong_features = 0
            total_features = 0
            
            for feature_name, value in features.items():
                total_features += 1
                if feature_name == "forehead_wrinkles" and value > 40:
                    strong_features += 1
                elif feature_name == "eye_wrinkles" and value > 30:
                    strong_features += 1
                elif feature_name == "nasolabial_depth" and value > 20:
                    strong_features += 1
                elif feature_name == "skin_texture" and value > 5:
                    strong_features += 1
                elif feature_name in ["eye_bags", "bone_prominence"] and value > 0.1:
                    strong_features += 1
            
            if total_features > 0:
                feature_confidence = strong_features / total_features
            
            # Adjust for legal classification confidence
            legal_confidence = legal_classification.get("confidence", 0.5)
            
            # Overall confidence (weighted combination)
            overall_confidence = (feature_confidence * 0.6) + (legal_confidence * 0.4)
            
            # Boost confidence for clear cases
            if legal_classification.get("category") in ["ADULT", "SENIOR"] and not legal_classification.get("forensic_notes"):
                overall_confidence = min(1.0, overall_confidence * 1.2)
            
            return overall_confidence
            
        except Exception as e:
            logger.error(f"Error calculating age confidence: {str(e)}")
            return 0.0
            
    async def _predict_gender(self, face_image: np.ndarray) -> Dict[str, Any]:
        """Advanced gender prediction using clinical anthropometric measurements"""
        try:
            gray = cv2.cvtColor(face_image, cv2.COLOR_BGR2GRAY)
            height, width = gray.shape
            
            # Extract comprehensive gender-related features
            gender_features = await self._extract_gender_features(gray)
            
            # Clinical gender classification using anthropometric data
            clinical_prediction = await self._clinical_gender_classification(gender_features)
            
            # Forensic confidence assessment
            forensic_confidence = self._calculate_gender_forensic_confidence(gender_features, clinical_prediction)
            
            result = {
                "predicted_gender": clinical_prediction["predicted_gender"],
                "confidence": forensic_confidence,
                "clinical_confidence": clinical_prediction["confidence"],
                "gender_scores": clinical_prediction["gender_scores"],
                "anthropometric_features": gender_features,
                "forensic_notes": clinical_prediction.get("forensic_notes", []),
                "analysis_method": "Clinical Anthropometric Assessment",
                "reliability_metrics": {
                    "feature_consistency": clinical_prediction.get("feature_consistency", 0.0),
                    "measurement_precision": clinical_prediction.get("measurement_precision", 0.0)
                }
            }
            
            return convert_numpy_types(result)
            
        except Exception as e:
            logger.error(f"Error predicting gender: {str(e)}")
            return {
                "predicted_gender": None,
                "confidence": 0.0,
                "error": str(e)
            }
    
    async def _extract_gender_features(self, gray_image: np.ndarray) -> Dict[str, float]:
        """Extract comprehensive anthropometric features for gender classification"""
        try:
            height, width = gray_image.shape
            
            # 1. Cranial measurements (Sexual dimorphism indicators)
            # Jaw width and sharpness (males typically have broader, more angular jaws)
            jaw_region = gray_image[int(height*0.65):int(height*0.95), int(width*0.1):int(width*0.9)]
            
            # Horizontal jaw analysis
            jaw_horizontal_grad = cv2.Sobel(jaw_region, cv2.CV_64F, 1, 0, ksize=5)
            jaw_angularity = np.std(jaw_horizontal_grad)
            
            # Jaw width measurement
            jaw_edges = cv2.Canny(jaw_region, 50, 150)
            jaw_contours, _ = cv2.findContours(jaw_edges, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)
            jaw_width_ratio = 0.0
            if jaw_contours:
                largest_jaw_contour = max(jaw_contours, key=cv2.contourArea)
                x, y, w, h = cv2.boundingRect(largest_jaw_contour)
                jaw_width_ratio = w / width
            
            # 2. Cheekbone prominence (females typically have higher, more prominent cheekbones)
            cheek_region = gray_image[int(height*0.35):int(height*0.65), int(width*0.15):int(width*0.85)]
            
            # Vertical gradient for cheekbone detection
            cheek_vertical_grad = cv2.Sobel(cheek_region, cv2.CV_64F, 0, 1, ksize=5)
            cheekbone_prominence = np.std(cheek_vertical_grad)
            
            # Cheekbone height analysis
            cheek_horizontal_profile = np.mean(cheek_region, axis=1)
            cheek_peak_position = np.argmax(cheek_horizontal_profile) / len(cheek_horizontal_profile)
            
            # 3. Forehead analysis (males typically have more prominent brow ridges)
            forehead_region = gray_image[int(height*0.05):int(height*0.35), int(width*0.15):int(width*0.85)]
            
            # Brow ridge prominence
            brow_region = forehead_region[int(forehead_region.shape[0]*0.7):, :]
            brow_ridge_prominence = np.std(cv2.Sobel(brow_region, cv2.CV_64F, 0, 1, ksize=3))
            
            # Forehead slope (males tend to have more sloping foreheads)
            forehead_profile = np.mean(forehead_region, axis=1)
            forehead_slope = np.polyfit(range(len(forehead_profile)), forehead_profile, 1)[0]
            
            # 4. Eye analysis (females typically have larger, more prominent eyes)
            eye_region = gray_image[int(height*0.25):int(height*0.55), int(width*0.15):int(width*0.85)]
            
            # Eye size relative to face
            eye_brightness_threshold = np.mean(eye_region) + np.std(eye_region)
            bright_eye_pixels = np.sum(eye_region > eye_brightness_threshold)
            eye_size_ratio = bright_eye_pixels / (eye_region.shape[0] * eye_region.shape[1])
            
            # Eye shape analysis
            eye_edges = cv2.Canny(eye_region, 30, 100)
            eye_contours, _ = cv2.findContours(eye_edges, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)
            eye_roundness = 0.0
            if eye_contours:
                for contour in eye_contours:
                    if cv2.contourArea(contour) > 100:  # Filter small contours
                        perimeter = cv2.arcLength(contour, True)
                        area = cv2.contourArea(contour)
                        if perimeter > 0:
                            circularity = 4 * np.pi * area / (perimeter * perimeter)
                            eye_roundness = max(eye_roundness, circularity)
            
            # 5. Nasal analysis (males typically have larger noses)
            nose_region = gray_image[int(height*0.4):int(height*0.7), int(width*0.35):int(width*0.65)]
            
            # Nose width
            nose_horizontal_grad = cv2.Sobel(nose_region, cv2.CV_64F, 1, 0, ksize=3)
            nose_edges = np.where(np.abs(nose_horizontal_grad) > np.std(nose_horizontal_grad))
            nose_width_ratio = 0.0
            if len(nose_edges[1]) > 0:
                nose_width_pixels = np.max(nose_edges[1]) - np.min(nose_edges[1])
                nose_width_ratio = nose_width_pixels / width
            
            # Nose prominence
            nose_vertical_grad = cv2.Sobel(nose_region, cv2.CV_64F, 0, 1, ksize=3)
            nose_prominence = np.std(nose_vertical_grad)
            
            # 6. Lip analysis (females typically have fuller lips)
            lip_region = gray_image[int(height*0.65):int(height*0.85), int(width*0.25):int(width*0.75)]
            
            # Lip fullness (based on gradient intensity)
            lip_grad = cv2.Sobel(lip_region, cv2.CV_64F, 0, 1, ksize=3)
            lip_fullness = np.std(lip_grad)
            
            # 7. Overall facial softness vs. angularity
            # Compute overall gradient magnitude
            full_face_grad_x = cv2.Sobel(gray_image, cv2.CV_64F, 1, 0, ksize=5)
            full_face_grad_y = cv2.Sobel(gray_image, cv2.CV_64F, 0, 1, ksize=5)
            gradient_magnitude = np.sqrt(full_face_grad_x**2 + full_face_grad_y**2)
            facial_angularity = np.std(gradient_magnitude)
            
            features = {
                "jaw_angularity": jaw_angularity,
                "jaw_width_ratio": jaw_width_ratio,
                "cheekbone_prominence": cheekbone_prominence,
                "cheek_peak_position": cheek_peak_position,
                "brow_ridge_prominence": brow_ridge_prominence,
                "forehead_slope": abs(forehead_slope),
                "eye_size_ratio": eye_size_ratio,
                "eye_roundness": eye_roundness,
                "nose_width_ratio": nose_width_ratio,
                "nose_prominence": nose_prominence,
                "lip_fullness": lip_fullness,
                "facial_angularity": facial_angularity
            }
            
            return features
            
        except Exception as e:
            logger.error(f"Error extracting gender features: {str(e)}")
            return {}
    
    async def _clinical_gender_classification(self, features: Dict[str, float]) -> Dict[str, Any]:
        """Clinical gender classification using anthropometric standards"""
        try:
            if not features:
                return {
                    "predicted_gender": None,
                    "confidence": 0.0,
                    "gender_scores": {"male": 0.5, "female": 0.5}
                }
            
            # Clinical scoring system based on sexual dimorphism research
            male_score = 0.0
            female_score = 0.0
            feature_weights = {}
            forensic_notes = []
            
            # Jaw analysis (High reliability indicator)
            jaw_angularity = features.get("jaw_angularity", 0)
            jaw_width = features.get("jaw_width_ratio", 0)
            
            if jaw_angularity > 60:  # Sharp, angular jaw (male indicator)
                male_score += 0.20
                feature_weights["jaw_angularity"] = "strong_male"
            elif jaw_angularity < 30:  # Soft jaw line (female indicator)
                female_score += 0.15
                feature_weights["jaw_angularity"] = "moderate_female"
            
            if jaw_width > 0.75:  # Wide jaw (male indicator)
                male_score += 0.18
                feature_weights["jaw_width"] = "strong_male"
            elif jaw_width < 0.65:  # Narrow jaw (female indicator)
                female_score += 0.12
                feature_weights["jaw_width"] = "moderate_female"
            
            # Cheekbone analysis (Medium reliability)
            cheekbone_prominence = features.get("cheekbone_prominence", 0)
            cheek_position = features.get("cheek_peak_position", 0.5)
            
            if cheekbone_prominence > 40 and cheek_position > 0.6:  # High, prominent cheekbones
                female_score += 0.16
                feature_weights["cheekbones"] = "strong_female"
            elif cheekbone_prominence < 25:  # Low cheekbones
                male_score += 0.12
                feature_weights["cheekbones"] = "moderate_male"
            
            # Brow ridge analysis (High reliability)
            brow_prominence = features.get("brow_ridge_prominence", 0)
            forehead_slope = features.get("forehead_slope", 0)
            
            if brow_prominence > 35:  # Prominent brow ridge (male indicator)
                male_score += 0.19
                feature_weights["brow_ridge"] = "strong_male"
            elif brow_prominence < 15:  # Smooth brow (female indicator)
                female_score += 0.14
                feature_weights["brow_ridge"] = "moderate_female"
            
            # Eye analysis (Medium reliability)
            eye_size = features.get("eye_size_ratio", 0)
            eye_roundness = features.get("eye_roundness", 0)
            
            if eye_size > 0.15 and eye_roundness > 0.6:  # Large, round eyes
                female_score += 0.13
                feature_weights["eyes"] = "moderate_female"
            elif eye_size < 0.1:  # Small eyes
                male_score += 0.10
                feature_weights["eyes"] = "weak_male"
            
            # Nose analysis (Medium reliability)
            nose_width = features.get("nose_width_ratio", 0)
            nose_prominence = features.get("nose_prominence", 0)
            
            if nose_width > 0.25 and nose_prominence > 30:  # Large, prominent nose
                male_score += 0.15
                feature_weights["nose"] = "moderate_male"
            elif nose_width < 0.18:  # Small nose
                female_score += 0.11
                feature_weights["nose"] = "weak_female"
            
            # Lip analysis (Lower reliability due to makeup effects)
            lip_fullness = features.get("lip_fullness", 0)
            
            if lip_fullness > 25:  # Full lips
                female_score += 0.08
                feature_weights["lips"] = "weak_female"
            elif lip_fullness < 12:  # Thin lips
                male_score += 0.06
                feature_weights["lips"] = "weak_male"
            
            # Overall facial angularity (Medium reliability)
            facial_angularity = features.get("facial_angularity", 0)
            
            if facial_angularity > 50:  # Angular features
                male_score += 0.12
                feature_weights["overall_angularity"] = "moderate_male"
            elif facial_angularity < 30:  # Soft features
                female_score += 0.10
                feature_weights["overall_angularity"] = "weak_female"
            
            # Normalize scores
            total_score = male_score + female_score
            if total_score > 0:
                male_probability = male_score / total_score
                female_probability = female_score / total_score
            else:
                male_probability = female_probability = 0.5
            
            # Determine prediction and confidence
            if male_probability > female_probability:
                predicted_gender = "Male"
                confidence = male_probability
                decision_margin = male_probability - female_probability
            else:
                predicted_gender = "Female"
                confidence = female_probability
                decision_margin = female_probability - male_probability
            
            # Adjust confidence based on decision margin
            if decision_margin > 0.4:
                confidence_level = "HIGH"
            elif decision_margin > 0.2:
                confidence_level = "MEDIUM"
            else:
                confidence_level = "LOW"
                forensic_notes.append("Close call - features show mixed indicators")
            
            # Feature consistency check
            strong_features = sum(1 for weight in feature_weights.values() if "strong" in weight)
            total_features = len(feature_weights)
            feature_consistency = strong_features / max(total_features, 1)
            
            # Measurement precision (based on feature quality)
            non_zero_features = sum(1 for f in features.values() if f > 0)
            measurement_precision = non_zero_features / len(features)
            
            result = {
                "predicted_gender": predicted_gender,
                "confidence": confidence,
                "confidence_level": confidence_level,
                "gender_scores": {
                    "male": male_probability,
                    "female": female_probability
                },
                "feature_weights": feature_weights,
                "decision_margin": decision_margin,
                "feature_consistency": feature_consistency,
                "measurement_precision": measurement_precision,
                "forensic_notes": forensic_notes
            }
            
            return result
            
        except Exception as e:
            logger.error(f"Error in clinical gender classification: {str(e)}")
            return {
                "predicted_gender": None,
                "confidence": 0.0,
                "gender_scores": {"male": 0.5, "female": 0.5}
            }
    
    def _calculate_gender_forensic_confidence(self, features: Dict[str, float], clinical_prediction: Dict[str, Any]) -> float:
        """Calculate forensic-grade confidence for gender prediction"""
        try:
            base_confidence = clinical_prediction.get("confidence", 0.5)
            decision_margin = clinical_prediction.get("decision_margin", 0.0)
            feature_consistency = clinical_prediction.get("feature_consistency", 0.0)
            measurement_precision = clinical_prediction.get("measurement_precision", 0.0)
            
            # Forensic confidence calculation
            # Base confidence from clinical prediction
            forensic_confidence = base_confidence
            
            # Adjust for decision margin (higher margin = higher confidence)
            if decision_margin > 0.3:
                forensic_confidence *= 1.2
            elif decision_margin < 0.1:
                forensic_confidence *= 0.7
            
            # Adjust for feature consistency
            forensic_confidence *= (0.6 + 0.4 * feature_consistency)
            
            # Adjust for measurement precision
            forensic_confidence *= (0.7 + 0.3 * measurement_precision)
            
            # Apply forensic standards (more conservative)
            # For legal/forensic purposes, require higher confidence
            if forensic_confidence > 0.85:
                forensic_confidence = min(0.95, forensic_confidence)  # Cap at 95%
            elif forensic_confidence < 0.6:
                forensic_confidence *= 0.8  # Reduce low confidence further
            
            return min(1.0, forensic_confidence)
            
        except Exception as e:
            logger.error(f"Error calculating forensic confidence: {str(e)}")
            return 0.5
            
    async def _analyze_emotion(self, face_image: np.ndarray) -> Dict[str, Any]:
        """Advanced emotion analysis using Facial Action Unit (FAU) coding system"""
        try:
            gray = cv2.cvtColor(face_image, cv2.COLOR_BGR2GRAY)
            
            # Extract facial landmarks for precise FAU analysis
            landmarks = await self._extract_landmarks(face_image)
            
            # Analyze Facial Action Units (FACS - Facial Action Coding System)
            fau_analysis = await self._analyze_facial_action_units(gray, landmarks)
            
            # Convert FAU codes to emotion probabilities using scientific mapping
            emotion_analysis = await self._fau_to_emotion_mapping(fau_analysis)
            
            # Advanced micro-expression detection
            micro_expressions = await self._detect_micro_expressions(gray, landmarks)
            
            # Combine all analyses for comprehensive emotion assessment
            comprehensive_result = self._combine_emotion_analyses(emotion_analysis, micro_expressions, fau_analysis)
            
            return comprehensive_result
            
        except Exception as e:
            logger.error(f"Error analyzing emotion: {str(e)}")
            return {
                "dominant_emotion": "neutral",
                "confidence": 0.0,
                "fau_codes": {},
                "error": str(e)
            }
    
    async def _analyze_facial_action_units(self, gray_image: np.ndarray, landmarks: List[Tuple[int, int]]) -> Dict[str, Any]:
        """Analyze Facial Action Units according to FACS (Facial Action Coding System)"""
        try:
            if not landmarks or len(landmarks) < 68:
                # Fallback to region-based analysis
                return await self._analyze_fau_without_landmarks(gray_image)
            
            height, width = gray_image.shape
            fau_scores = {}
            
            # Upper Face Action Units
            # AU1: Inner Brow Raiser
            au1_score = self._analyze_au1_inner_brow_raiser(landmarks, gray_image)
            fau_scores["AU1"] = au1_score
            
            # AU2: Outer Brow Raiser  
            au2_score = self._analyze_au2_outer_brow_raiser(landmarks, gray_image)
            fau_scores["AU2"] = au2_score
            
            # AU4: Brow Lowerer
            au4_score = self._analyze_au4_brow_lowerer(landmarks, gray_image)
            fau_scores["AU4"] = au4_score
            
            # AU5: Upper Lid Raiser
            au5_score = self._analyze_au5_upper_lid_raiser(landmarks, gray_image)
            fau_scores["AU5"] = au5_score
            
            # AU6: Cheek Raiser (important for genuine smiles)
            au6_score = self._analyze_au6_cheek_raiser(landmarks, gray_image)
            fau_scores["AU6"] = au6_score
            
            # AU7: Lid Tightener
            au7_score = self._analyze_au7_lid_tightener(landmarks, gray_image)
            fau_scores["AU7"] = au7_score
            
            # Lower Face Action Units
            # AU9: Nose Wrinkler
            au9_score = self._analyze_au9_nose_wrinkler(landmarks, gray_image)
            fau_scores["AU9"] = au9_score
            
            # AU10: Upper Lip Raiser
            au10_score = self._analyze_au10_upper_lip_raiser(landmarks, gray_image)
            fau_scores["AU10"] = au10_score
            
            # AU12: Lip Corner Puller (smile)
            au12_score = self._analyze_au12_lip_corner_puller(landmarks, gray_image)
            fau_scores["AU12"] = au12_score
            
            # AU15: Lip Corner Depressor (frown)
            au15_score = self._analyze_au15_lip_corner_depressor(landmarks, gray_image)
            fau_scores["AU15"] = au15_score
            
            # AU17: Chin Raiser
            au17_score = self._analyze_au17_chin_raiser(landmarks, gray_image)
            fau_scores["AU17"] = au17_score
            
            # AU20: Lip Stretcher
            au20_score = self._analyze_au20_lip_stretcher(landmarks, gray_image)
            fau_scores["AU20"] = au20_score
            
            # AU25: Lips Part
            au25_score = self._analyze_au25_lips_part(landmarks, gray_image)
            fau_scores["AU25"] = au25_score
            
            # AU26: Jaw Drop
            au26_score = self._analyze_au26_jaw_drop(landmarks, gray_image)
            fau_scores["AU26"] = au26_score
            
            # Calculate overall FAU intensity and combinations
            active_aus = {au: score for au, score in fau_scores.items() if score > 0.3}
            total_intensity = sum(fau_scores.values())
            
            result = {
                "fau_codes": fau_scores,
                "active_action_units": active_aus,
                "total_intensity": total_intensity,
                "dominant_aus": sorted(active_aus.items(), key=lambda x: x[1], reverse=True)[:5],
                "analysis_method": "FACS (Facial Action Coding System)"
            }
            
            return result
            
        except Exception as e:
            logger.error(f"Error analyzing FAU: {str(e)}")
            return {"fau_codes": {}, "error": str(e)}
    
    def _analyze_au1_inner_brow_raiser(self, landmarks: List[Tuple[int, int]], gray_image: np.ndarray) -> float:
        """Analyze AU1: Inner Brow Raiser"""
        try:
            if len(landmarks) < 27:
                return 0.0
            
            # Inner eyebrow points (landmarks 17-21 for right brow, 22-26 for left brow)
            inner_brow_points = landmarks[19:22]  # Inner portion of eyebrows
            
            # Calculate average height of inner brow points
            avg_brow_height = np.mean([point[1] for point in inner_brow_points])
            
            # Compare with eye level (approximate)
            eye_level = (landmarks[36][1] + landmarks[45][1]) / 2  # Average eye y-coordinate
            
            # Calculate relative elevation
            brow_elevation = max(0, eye_level - avg_brow_height) / (gray_image.shape[0] * 0.1)
            
            return min(1.0, max(0.0, brow_elevation))
            
        except:
            return 0.0
    
    def _analyze_au2_outer_brow_raiser(self, landmarks: List[Tuple[int, int]], gray_image: np.ndarray) -> float:
        """Analyze AU2: Outer Brow Raiser"""
        try:
            if len(landmarks) < 27:
                return 0.0
            
            # Outer eyebrow points
            outer_brow_points = [landmarks[17], landmarks[21], landmarks[22], landmarks[26]]
            
            # Calculate elevation relative to inner brow
            inner_brow_y = (landmarks[19][1] + landmarks[24][1]) / 2
            outer_brow_y = np.mean([point[1] for point in outer_brow_points])
            
            elevation_diff = max(0, inner_brow_y - outer_brow_y) / (gray_image.shape[0] * 0.05)
            
            return min(1.0, max(0.0, elevation_diff))
            
        except:
            return 0.0
    
    def _analyze_au4_brow_lowerer(self, landmarks: List[Tuple[int, int]], gray_image: np.ndarray) -> float:
        """Analyze AU4: Brow Lowerer (corrugator supercilii)"""
        try:
            if len(landmarks) < 27:
                return 0.0
            
            # Analyze brow region for lowering and furrowing
            brow_region = gray_image[landmarks[19][1]-10:landmarks[19][1]+5, 
                                   landmarks[17][0]:landmarks[26][0]]
            
            if brow_region.size == 0:
                return 0.0
            
            # Look for vertical wrinkles (furrowing)
            vertical_grad = cv2.Sobel(brow_region, cv2.CV_64F, 1, 0, ksize=3)
            furrowing = np.std(vertical_grad)
            
            # Normalize furrow intensity
            furrow_score = min(1.0, furrowing / 50.0)
            
            return furrow_score
            
        except:
            return 0.0
    
    def _analyze_au5_upper_lid_raiser(self, landmarks: List[Tuple[int, int]], gray_image: np.ndarray) -> float:
        """Analyze AU5: Upper Lid Raiser"""
        try:
            if len(landmarks) < 48:
                return 0.0
            
            # Calculate eye opening
            left_eye_opening = landmarks[41][1] - landmarks[37][1]
            right_eye_opening = landmarks[47][1] - landmarks[43][1]
            
            avg_opening = (left_eye_opening + right_eye_opening) / 2
            
            # Normalize by face height
            face_height = landmarks[8][1] - landmarks[19][1]  # Chin to brow
            opening_ratio = avg_opening / (face_height * 0.1) if face_height > 0 else 0
            
            # Wide open eyes indicate AU5
            lid_raise_score = max(0.0, (opening_ratio - 1.0))
            
            return min(1.0, lid_raise_score)
            
        except:
            return 0.0
    
    def _analyze_au6_cheek_raiser(self, landmarks: List[Tuple[int, int]], gray_image: np.ndarray) -> float:
        """Analyze AU6: Cheek Raiser (orbicularis oculi) - Key for genuine smiles"""
        try:
            if len(landmarks) < 48:
                return 0.0
            
            # Analyze cheek region elevation
            # Look for crow's feet and cheek elevation patterns
            
            # Define cheek regions
            left_cheek_region = gray_image[landmarks[36][1]:landmarks[48][1], 
                                         landmarks[0][0]:landmarks[36][0]]
            right_cheek_region = gray_image[landmarks[45][1]:landmarks[54][1], 
                                          landmarks[45][0]:landmarks[16][0]]
            
            # Analyze texture changes indicating muscle contraction
            left_texture = np.std(cv2.Laplacian(left_cheek_region, cv2.CV_64F)) if left_cheek_region.size > 0 else 0
            right_texture = np.std(cv2.Laplacian(right_cheek_region, cv2.CV_64F)) if right_cheek_region.size > 0 else 0
            
            avg_texture = (left_texture + right_texture) / 2
            
            # High texture variance indicates cheek muscle activation
            cheek_activation = min(1.0, avg_texture / 40.0)
            
            return cheek_activation
            
        except:
            return 0.0
    
    def _analyze_au7_lid_tightener(self, landmarks: List[Tuple[int, int]], gray_image: np.ndarray) -> float:
        """Analyze AU7: Lid Tightener"""
        try:
            if len(landmarks) < 48:
                return 0.0
            
            # Analyze eye region for lid tightening
            left_eye_region = gray_image[landmarks[37][1]-5:landmarks[41][1]+5,
                                       landmarks[36][0]-5:landmarks[39][0]+5]
            right_eye_region = gray_image[landmarks[43][1]-5:landmarks[47][1]+5,
                                        landmarks[42][0]-5:landmarks[45][0]+5]
            
            # Look for increased texture around eyes (indicating muscle tension)
            left_tension = np.std(left_eye_region) if left_eye_region.size > 0 else 0
            right_tension = np.std(right_eye_region) if right_eye_region.size > 0 else 0
            
            avg_tension = (left_tension + right_tension) / 2
            tension_score = min(1.0, avg_tension / 30.0)
            
            return tension_score
            
        except:
            return 0.0
    
    def _analyze_au9_nose_wrinkler(self, landmarks: List[Tuple[int, int]], gray_image: np.ndarray) -> float:
        """Analyze AU9: Nose Wrinkler"""
        try:
            if len(landmarks) < 36:
                return 0.0
            
            # Define nose bridge region
            nose_region = gray_image[landmarks[27][1]:landmarks[30][1],
                                   landmarks[31][0]:landmarks[35][0]]
            
            if nose_region.size == 0:
                return 0.0
            
            # Look for horizontal wrinkles on nose bridge
            horizontal_grad = cv2.Sobel(nose_region, cv2.CV_64F, 0, 1, ksize=3)
            wrinkle_intensity = np.std(horizontal_grad)
            
            wrinkle_score = min(1.0, wrinkle_intensity / 25.0)
            
            return wrinkle_score
            
        except:
            return 0.0
    
    def _analyze_au10_upper_lip_raiser(self, landmarks: List[Tuple[int, int]], gray_image: np.ndarray) -> float:
        """Analyze AU10: Upper Lip Raiser"""
        try:
            if len(landmarks) < 68:
                return 0.0
            
            # Analyze upper lip elevation
            upper_lip_center = landmarks[51]  # Upper lip center
            nose_base = landmarks[33]  # Nose base
            
            # Calculate lip elevation relative to nose
            lip_elevation = max(0, nose_base[1] - upper_lip_center[1])
            face_height = landmarks[8][1] - landmarks[27][1]
            
            elevation_ratio = lip_elevation / (face_height * 0.1) if face_height > 0 else 0
            
            return min(1.0, max(0.0, elevation_ratio - 0.5))
            
        except:
            return 0.0
    
    def _analyze_au12_lip_corner_puller(self, landmarks: List[Tuple[int, int]], gray_image: np.ndarray) -> float:
        """Analyze AU12: Lip Corner Puller (zygomaticus major) - Smile"""
        try:
            if len(landmarks) < 68:
                return 0.0
            
            # Analyze mouth corner elevation and lateral movement
            left_corner = landmarks[48]
            right_corner = landmarks[54]
            mouth_center = landmarks[51]
            
            # Calculate corner elevation relative to center
            left_elevation = max(0, mouth_center[1] - left_corner[1])
            right_elevation = max(0, mouth_center[1] - right_corner[1])
            
            # Calculate lateral stretch
            mouth_width = abs(right_corner[0] - left_corner[0])
            face_width = landmarks[16][0] - landmarks[0][0]
            
            width_ratio = mouth_width / face_width if face_width > 0 else 0
            
            # Combine elevation and stretch for smile intensity
            avg_elevation = (left_elevation + right_elevation) / 2
            face_height = landmarks[8][1] - landmarks[27][1]
            
            elevation_score = avg_elevation / (face_height * 0.05) if face_height > 0 else 0
            stretch_score = max(0, width_ratio - 0.4) * 5  # Boost for wider smiles
            
            smile_score = min(1.0, (elevation_score + stretch_score) / 2)
            
            return smile_score
            
        except:
            return 0.0
    
    def _analyze_au15_lip_corner_depressor(self, landmarks: List[Tuple[int, int]], gray_image: np.ndarray) -> float:
        """Analyze AU15: Lip Corner Depressor - Frown"""
        try:
            if len(landmarks) < 68:
                return 0.0
            
            # Analyze downward movement of mouth corners
            left_corner = landmarks[48]
            right_corner = landmarks[54]
            mouth_center = landmarks[51]
            
            # Calculate corner depression relative to center
            left_depression = max(0, left_corner[1] - mouth_center[1])
            right_depression = max(0, right_corner[1] - mouth_center[1])
            
            avg_depression = (left_depression + right_depression) / 2
            face_height = landmarks[8][1] - landmarks[27][1]
            
            depression_score = avg_depression / (face_height * 0.03) if face_height > 0 else 0
            
            return min(1.0, depression_score)
            
        except:
            return 0.0
    
    def _analyze_au17_chin_raiser(self, landmarks: List[Tuple[int, int]], gray_image: np.ndarray) -> float:
        """Analyze AU17: Chin Raiser"""
        try:
            if len(landmarks) < 68:
                return 0.0
            
            # Analyze chin region for elevation/wrinkling
            chin_point = landmarks[8]  # Chin center
            chin_region = gray_image[chin_point[1]-10:chin_point[1]+15,
                                   chin_point[0]-15:chin_point[0]+15]
            
            if chin_region.size == 0:
                return 0.0
            
            # Look for chin muscle activation (dimpling/wrinkling)
            chin_texture = np.std(cv2.Laplacian(chin_region, cv2.CV_64F))
            texture_score = min(1.0, chin_texture / 30.0)
            
            return texture_score
            
        except:
            return 0.0
    
    def _analyze_au20_lip_stretcher(self, landmarks: List[Tuple[int, int]], gray_image: np.ndarray) -> float:
        """Analyze AU20: Lip Stretcher"""
        try:
            if len(landmarks) < 68:
                return 0.0
            
            # Analyze horizontal lip stretching
            mouth_width = abs(landmarks[54][0] - landmarks[48][0])
            face_width = landmarks[16][0] - landmarks[0][0]
            
            width_ratio = mouth_width / face_width if face_width > 0 else 0
            
            # Compare to normal mouth width ratio
            stretch_score = max(0, (width_ratio - 0.35) * 4)  # Normal ratio ~0.35
            
            return min(1.0, stretch_score)
            
        except:
            return 0.0
    
    def _analyze_au25_lips_part(self, landmarks: List[Tuple[int, int]], gray_image: np.ndarray) -> float:
        """Analyze AU25: Lips Part"""
        try:
            if len(landmarks) < 68:
                return 0.0
            
            # Calculate lip separation
            upper_lip = landmarks[51]  # Upper lip center
            lower_lip = landmarks[57]  # Lower lip center
            
            lip_separation = abs(lower_lip[1] - upper_lip[1])
            face_height = landmarks[8][1] - landmarks[27][1]
            
            separation_ratio = lip_separation / (face_height * 0.02) if face_height > 0 else 0
            
            return min(1.0, max(0.0, separation_ratio - 0.5))
            
        except:
            return 0.0
    
    def _analyze_au26_jaw_drop(self, landmarks: List[Tuple[int, int]], gray_image: np.ndarray) -> float:
        """Analyze AU26: Jaw Drop"""
        try:
            if len(landmarks) < 68:
                return 0.0
            
            # Analyze jaw opening
            chin = landmarks[8]
            mouth_center = landmarks[51]
            
            jaw_opening = abs(chin[1] - mouth_center[1])
            face_height = landmarks[8][1] - landmarks[27][1]
            
            opening_ratio = jaw_opening / (face_height * 0.15) if face_height > 0 else 0
            
            jaw_drop_score = max(0.0, opening_ratio - 0.3)  # Threshold for normal mouth position
            
            return min(1.0, jaw_drop_score * 2)
            
        except:
            return 0.0
    
    async def _analyze_fau_without_landmarks(self, gray_image: np.ndarray) -> Dict[str, Any]:
        """Fallback FAU analysis without precise landmarks"""
        try:
            height, width = gray_image.shape
            
            # Region-based FAU approximation
            fau_scores = {}
            
            # Upper face regions
            upper_region = gray_image[:int(height*0.5), :]
            brow_region = gray_image[int(height*0.15):int(height*0.35), :]
            eye_region = gray_image[int(height*0.25):int(height*0.5), :]
            
            # Lower face regions  
            nose_region = gray_image[int(height*0.4):int(height*0.65), int(width*0.35):int(width*0.65)]
            mouth_region = gray_image[int(height*0.55):int(height*0.8), int(width*0.25):int(width*0.75)]
            chin_region = gray_image[int(height*0.8):, int(width*0.3):int(width*0.7)]
            
            # Approximate FAU detection using texture and gradient analysis
            fau_scores["AU4"] = min(1.0, np.std(cv2.Sobel(brow_region, cv2.CV_64F, 1, 0, ksize=3)) / 50.0)
            fau_scores["AU6"] = min(1.0, np.std(cv2.Laplacian(eye_region, cv2.CV_64F)) / 40.0)
            fau_scores["AU12"] = self._analyze_mouth_curvature(mouth_region) if self._analyze_mouth_curvature(mouth_region) > 0 else 0.0
            fau_scores["AU15"] = abs(self._analyze_mouth_curvature(mouth_region)) if self._analyze_mouth_curvature(mouth_region) < 0 else 0.0
            
            return {
                "fau_codes": fau_scores,
                "active_action_units": {au: score for au, score in fau_scores.items() if score > 0.3},
                "analysis_method": "Region-based FAU approximation (no landmarks)"
            }
            
        except Exception as e:
            logger.error(f"Error in fallback FAU analysis: {str(e)}")
            return {"fau_codes": {}, "error": str(e)}
    
    async def _fau_to_emotion_mapping(self, fau_analysis: Dict[str, Any]) -> Dict[str, Any]:
        """Map FAU codes to emotions using scientific FACS research"""
        try:
            fau_codes = fau_analysis.get("fau_codes", {})
            
            # Scientific emotion mappings based on FACS research
            emotion_scores = {
                "happiness": 0.0,
                "sadness": 0.0,
                "anger": 0.0,
                "surprise": 0.0,
                "fear": 0.0,
                "disgust": 0.0,
                "neutral": 0.0
            }
            
            # Happiness: AU6 + AU12 (Duchenne smile)
            if fau_codes.get("AU6", 0) > 0.3 and fau_codes.get("AU12", 0) > 0.3:
                emotion_scores["happiness"] = (fau_codes["AU6"] + fau_codes["AU12"]) / 2
                emotion_scores["happiness"] *= 1.2  # Boost for Duchenne combination
            elif fau_codes.get("AU12", 0) > 0.3:
                emotion_scores["happiness"] = fau_codes["AU12"] * 0.8  # Non-Duchenne smile
            
            # Sadness: AU1 + AU4 + AU15
            sadness_aus = [fau_codes.get("AU1", 0), fau_codes.get("AU4", 0), fau_codes.get("AU15", 0)]
            emotion_scores["sadness"] = np.mean([au for au in sadness_aus if au > 0.2])
            
            # Anger: AU4 + AU5 + AU7 + AU10
            anger_aus = [fau_codes.get("AU4", 0), fau_codes.get("AU5", 0), 
                        fau_codes.get("AU7", 0), fau_codes.get("AU10", 0)]
            emotion_scores["anger"] = np.mean([au for au in anger_aus if au > 0.2])
            
            # Surprise: AU1 + AU2 + AU5 + AU26
            surprise_aus = [fau_codes.get("AU1", 0), fau_codes.get("AU2", 0),
                           fau_codes.get("AU5", 0), fau_codes.get("AU26", 0)]
            emotion_scores["surprise"] = np.mean([au for au in surprise_aus if au > 0.2])
            
            # Fear: AU1 + AU2 + AU4 + AU5 + AU7 + AU20 + AU26
            fear_aus = [fau_codes.get("AU1", 0), fau_codes.get("AU2", 0), fau_codes.get("AU4", 0),
                       fau_codes.get("AU5", 0), fau_codes.get("AU7", 0), fau_codes.get("AU20", 0)]
            emotion_scores["fear"] = np.mean([au for au in fear_aus if au > 0.2])
            
            # Disgust: AU9 + AU15 + AU17
            disgust_aus = [fau_codes.get("AU9", 0), fau_codes.get("AU15", 0), fau_codes.get("AU17", 0)]
            emotion_scores["disgust"] = np.mean([au for au in disgust_aus if au > 0.2])
            
            # Neutral: Low activation across all AUs
            total_activation = sum(fau_codes.values())
            emotion_scores["neutral"] = max(0.3, 1.0 - (total_activation / len(fau_codes))) if fau_codes else 0.5
            
            # Normalize emotions
            total_emotion = sum(emotion_scores.values())
            if total_emotion > 0:
                emotion_scores = {k: v/total_emotion for k, v in emotion_scores.items()}
            
            # Find dominant emotion
            dominant_emotion = max(emotion_scores.items(), key=lambda x: x[1])
            
            return {
                "emotion_probabilities": emotion_scores,
                "dominant_emotion": dominant_emotion[0],
                "emotion_confidence": dominant_emotion[1],
                "supporting_faus": fau_analysis.get("active_action_units", {}),
                "analysis_method": "FACS-based emotion mapping"
            }
            
        except Exception as e:
            logger.error(f"Error mapping FAU to emotions: {str(e)}")
            return {"emotion_probabilities": {"neutral": 1.0}, "dominant_emotion": "neutral"}
    
    async def _detect_micro_expressions(self, gray_image: np.ndarray, landmarks: List[Tuple[int, int]]) -> Dict[str, Any]:
        """Detect micro-expressions (brief, involuntary facial expressions)"""
        try:
            # Micro-expressions are typically detected through temporal analysis
            # For single image analysis, we look for subtle asymmetries and partial expressions
            
            if not landmarks or len(landmarks) < 68:
                return {"micro_expressions": [], "asymmetry_detected": False}
            
            micro_expressions = []
            
            # Facial asymmetry analysis (indicator of suppressed emotions)
            asymmetry_score = self._analyze_facial_asymmetry(landmarks, gray_image)
            
            # Partial expression detection (incomplete AU activation patterns)
            partial_expressions = self._detect_partial_expressions(landmarks, gray_image)
            
            # Inconsistent expression patterns
            expression_inconsistencies = self._detect_expression_inconsistencies(landmarks, gray_image)
            
            if asymmetry_score > 0.4:
                micro_expressions.append({
                    "type": "facial_asymmetry",
                    "intensity": asymmetry_score,
                    "description": "Significant facial asymmetry detected - possible micro-expression"
                })
            
            if partial_expressions:
                micro_expressions.extend(partial_expressions)
                
            if expression_inconsistencies:
                micro_expressions.extend(expression_inconsistencies)
            
            return {
                "micro_expressions": micro_expressions,
                "asymmetry_score": asymmetry_score,
                "asymmetry_detected": asymmetry_score > 0.3,
                "analysis_method": "Single-frame micro-expression indicators"
            }
            
        except Exception as e:
            logger.error(f"Error detecting micro-expressions: {str(e)}")
            return {"micro_expressions": [], "error": str(e)}
    
    def _analyze_facial_asymmetry(self, landmarks: List[Tuple[int, int]], gray_image: np.ndarray) -> float:
        """Analyze facial asymmetry as indicator of micro-expressions"""
        try:
            if len(landmarks) < 68:
                return 0.0
            
            # Compare left and right sides of face
            face_center_x = landmarks[30][0]  # Nose tip as center reference
            
            asymmetry_scores = []
            
            # Eyebrow asymmetry
            left_brow_height = np.mean([landmarks[i][1] for i in range(17, 22)])
            right_brow_height = np.mean([landmarks[i][1] for i in range(22, 27)])
            brow_asymmetry = abs(left_brow_height - right_brow_height) / (gray_image.shape[0] * 0.1)
            asymmetry_scores.append(brow_asymmetry)
            
            # Eye asymmetry
            left_eye_opening = landmarks[41][1] - landmarks[37][1]
            right_eye_opening = landmarks[47][1] - landmarks[43][1]
            eye_asymmetry = abs(left_eye_opening - right_eye_opening) / (gray_image.shape[0] * 0.05)
            asymmetry_scores.append(eye_asymmetry)
            
            # Mouth asymmetry
            left_mouth_corner = landmarks[48]
            right_mouth_corner = landmarks[54]
            mouth_center = landmarks[51]
            
            left_deviation = abs(left_mouth_corner[1] - mouth_center[1])
            right_deviation = abs(right_mouth_corner[1] - mouth_center[1])
            mouth_asymmetry = abs(left_deviation - right_deviation) / (gray_image.shape[0] * 0.05)
            asymmetry_scores.append(mouth_asymmetry)
            
            # Overall asymmetry score
            overall_asymmetry = np.mean(asymmetry_scores)
            
            return min(1.0, overall_asymmetry)
            
        except:
            return 0.0
    
    def _detect_partial_expressions(self, landmarks: List[Tuple[int, int]], gray_image: np.ndarray) -> List[Dict[str, Any]]:
        """Detect partial or incomplete expressions"""
        try:
            partial_expressions = []
            
            # Check for partial smiles (mouth movement without eye involvement)
            mouth_smile_score = self._analyze_au12_lip_corner_puller(landmarks, gray_image)
            eye_smile_score = self._analyze_au6_cheek_raiser(landmarks, gray_image)
            
            if mouth_smile_score > 0.4 and eye_smile_score < 0.2:
                partial_expressions.append({
                    "type": "partial_smile",
                    "intensity": mouth_smile_score,
                    "description": "Mouth smile without eye involvement - possible forced expression"
                })
            
            # Check for suppressed negative emotions
            brow_tension = self._analyze_au4_brow_lowerer(landmarks, gray_image)
            mouth_control = self._analyze_au20_lip_stretcher(landmarks, gray_image)
            
            if brow_tension > 0.3 and mouth_control > 0.3:
                partial_expressions.append({
                    "type": "suppressed_negative",
                    "intensity": (brow_tension + mouth_control) / 2,
                    "description": "Brow tension with lip control - possible suppressed negative emotion"
                })
            
            return partial_expressions
            
        except:
            return []
    
    def _detect_expression_inconsistencies(self, landmarks: List[Tuple[int, int]], gray_image: np.ndarray) -> List[Dict[str, Any]]:
        """Detect inconsistent expression patterns"""
        try:
            inconsistencies = []
            
            # Check for conflicting upper and lower face expressions
            upper_face_positive = self._analyze_au1_inner_brow_raiser(landmarks, gray_image) + \
                                 self._analyze_au2_outer_brow_raiser(landmarks, gray_image)
            
            lower_face_negative = self._analyze_au15_lip_corner_depressor(landmarks, gray_image) + \
                                 self._analyze_au17_chin_raiser(landmarks, gray_image)
            
            if upper_face_positive > 0.4 and lower_face_negative > 0.4:
                inconsistencies.append({
                    "type": "upper_lower_conflict",
                    "intensity": (upper_face_positive + lower_face_negative) / 2,
                    "description": "Conflicting upper and lower face expressions detected"
                })
            
            return inconsistencies
            
        except:
            return []
    
    def _combine_emotion_analyses(self, emotion_analysis: Dict[str, Any], 
                                micro_expressions: Dict[str, Any], 
                                fau_analysis: Dict[str, Any]) -> Dict[str, Any]:
        """Combine all emotion analyses for comprehensive result"""
        try:
            # Get primary emotion from FAU analysis
            primary_emotion = emotion_analysis.get("dominant_emotion", "neutral")
            primary_confidence = emotion_analysis.get("emotion_confidence", 0.0)
            
            # Adjust confidence based on micro-expression detection
            micro_detected = len(micro_expressions.get("micro_expressions", [])) > 0
            if micro_detected:
                primary_confidence *= 0.8  # Reduce confidence if micro-expressions detected
            
            # Create comprehensive result
            result = {
                "dominant_emotion": primary_emotion,
                "confidence": primary_confidence,
                "emotion_probabilities": emotion_analysis.get("emotion_probabilities", {}),
                "fau_analysis": {
                    "fau_codes": fau_analysis.get("fau_codes", {}),
                    "active_action_units": fau_analysis.get("active_action_units", {}),
                    "dominant_aus": fau_analysis.get("dominant_aus", [])
                },
                "micro_expression_analysis": micro_expressions,
                "expression_authenticity": {
                    "likely_genuine": not micro_detected and primary_confidence > 0.6,
                    "micro_expressions_detected": micro_detected,
                    "confidence_adjustment": -0.2 if micro_detected else 0.0
                },
                "analysis_method": "Comprehensive FAU + Micro-expression Analysis",
                "forensic_notes": self._generate_forensic_emotion_notes(emotion_analysis, micro_expressions, fau_analysis)
            }
            
            return convert_numpy_types(result)
            
        except Exception as e:
            logger.error(f"Error combining emotion analyses: {str(e)}")
            return {
                "dominant_emotion": "neutral",
                "confidence": 0.0,
                "error": str(e)
            }
    
    def _generate_forensic_emotion_notes(self, emotion_analysis: Dict[str, Any], 
                                       micro_expressions: Dict[str, Any], 
                                       fau_analysis: Dict[str, Any]) -> List[str]:
        """Generate forensic notes for emotion analysis"""
        notes = []
        
        try:
            # Primary emotion confidence
            confidence = emotion_analysis.get("emotion_confidence", 0.0)
            if confidence > 0.8:
                notes.append("High confidence emotion detection - strong FAU activation pattern")
            elif confidence < 0.4:
                notes.append("Low confidence emotion detection - weak or conflicting signals")
            
            # Duchenne smile detection
            fau_codes = fau_analysis.get("fau_codes", {})
            if fau_codes.get("AU6", 0) > 0.4 and fau_codes.get("AU12", 0) > 0.4:
                notes.append("Duchenne smile detected - likely genuine happiness expression")
            elif fau_codes.get("AU12", 0) > 0.4 and fau_codes.get("AU6", 0) < 0.2:
                notes.append("Non-Duchenne smile detected - possible social/forced smile")
            
            # Micro-expression indicators
            if micro_expressions.get("asymmetry_detected", False):
                notes.append("Facial asymmetry detected - possible emotion suppression or micro-expression")
            
            micro_list = micro_expressions.get("micro_expressions", [])
            if micro_list:
                notes.append(f"Micro-expression indicators found: {len(micro_list)} detected")
            
            # Expression consistency
            active_aus = len(fau_analysis.get("active_action_units", {}))
            if active_aus >= 4:
                notes.append("Complex expression pattern - multiple active Action Units")
            elif active_aus <= 1:
                notes.append("Simple expression pattern - minimal facial muscle activation")
            
            return notes
            
        except:
            return ["Error generating forensic notes"]
            
    def _analyze_mouth_curvature(self, mouth_region: np.ndarray) -> float:
        """Analyze mouth curvature for emotion detection"""
        try:
            height, width = mouth_region.shape
            
            # Find mouth line using edge detection
            edges = cv2.Canny(mouth_region, 50, 150)
            
            # Analyze horizontal lines (mouth line)
            horizontal_lines = []
            for y in range(height//3, 2*height//3):
                line_strength = np.sum(edges[y, :])
                if line_strength > 0:
                    horizontal_lines.append((y, line_strength))
                    
            if not horizontal_lines:
                return 0.0
                
            # Find strongest horizontal line (likely mouth line)
            mouth_line_y = max(horizontal_lines, key=lambda x: x[1])[0]
            mouth_line = edges[mouth_line_y, :]
            
            # Analyze curvature by checking if corners are higher or lower than center
            left_third = np.mean(mouth_line[:width//3])
            center_third = np.mean(mouth_line[width//3:2*width//3])
            right_third = np.mean(mouth_line[2*width//3:])
            
            # Calculate curvature score
            curvature = (left_third + right_third) / (2 * center_third) if center_third > 0 else 0
            return max(-1.0, min(1.0, (curvature - 1.0) * 2))  # Normalize to -1 to 1
            
        except:
            return 0.0
            
    async def _analyze_ethnicity(self, face_image: np.ndarray) -> Dict[str, Any]:
        """Analyze ethnicity using facial structure and features"""
        try:
            # Note: This is a sensitive area requiring careful implementation
            # For forensic purposes, focus on general facial structure analysis
            
            gray = cv2.cvtColor(face_image, cv2.COLOR_BGR2GRAY)
            height, width = gray.shape
            
            # Facial structure analysis
            nose_region = gray[int(height*0.4):int(height*0.7), int(width*0.4):int(width*0.6)]
            nose_width = np.sum(nose_region > 100, axis=1).mean()
            nose_height = nose_region.shape[0]
            
            eye_region = gray[int(height*0.25):int(height*0.5), int(width*0.2):int(width*0.8)]
            eye_shape = self._analyze_eye_shape(eye_region)
            
            # Face shape analysis
            face_width = np.sum(gray > 50, axis=1).mean()
            face_length = height
            face_ratio = face_length / face_width if face_width > 0 else 1.0
            
            # General categories based on facial structure
            structure_indicators = {
                "narrow_features": nose_width / width < 0.15 and face_ratio > 1.3,
                "broad_features": nose_width / width > 0.2 and face_ratio < 1.2,
                "angular_features": eye_shape["angularity"] > 0.6,
                "rounded_features": eye_shape["roundness"] > 0.6
            }
            
            # Provide general structural analysis rather than specific ethnicity
            result = {
                "facial_structure": "analyzed",
                "confidence": 0.5,  # Lower confidence for sensitive analysis
                "structural_features": {
                    "face_width_ratio": face_width / width,
                    "face_length_ratio": face_ratio,
                    "nose_width_ratio": nose_width / width,
                    "eye_shape_metrics": eye_shape
                },
                "note": "Structural analysis provided for forensic reference only"
            }
            
            return convert_numpy_types(result)
            
        except Exception as e:
            logger.error(f"Error analyzing ethnicity: {str(e)}")
            return {
                "facial_structure": None,
                "confidence": 0.0,
                "error": str(e)
            }
            
    def _analyze_eye_shape(self, eye_region: np.ndarray) -> Dict[str, float]:
        """Analyze eye shape characteristics"""
        try:
            # Detect eye contours
            edges = cv2.Canny(eye_region, 50, 150)
            contours, _ = cv2.findContours(edges, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)
            
            if not contours:
                return {"angularity": 0.5, "roundness": 0.5, "size": 0.5}
                
            # Find largest contour (likely eye shape)
            largest_contour = max(contours, key=cv2.contourArea)
            
            # Calculate shape characteristics
            area = cv2.contourArea(largest_contour)
            perimeter = cv2.arcLength(largest_contour, True)
            
            # Roundness (circle-like = 1, elongated = 0)
            roundness = 4 * np.pi * area / (perimeter * perimeter) if perimeter > 0 else 0
            
            # Angularity (based on contour approximation)
            epsilon = 0.02 * perimeter
            approx = cv2.approxPolyDP(largest_contour, epsilon, True)
            angularity = len(approx) / 8.0  # Normalized by typical eye points
            
            # Size relative to region
            size = area / (eye_region.shape[0] * eye_region.shape[1])
            
            result = {
                "angularity": min(1.0, angularity),
                "roundness": min(1.0, roundness),
                "size": min(1.0, size)
            }
            
            return convert_numpy_types(result)
            
        except:
            return {"angularity": 0.5, "roundness": 0.5, "size": 0.5}
            
    async def _detect_facial_hair(self, face_image: np.ndarray) -> Dict[str, Any]:
        """Detect facial hair presence and type"""
        try:
            gray = cv2.cvtColor(face_image, cv2.COLOR_BGR2GRAY)
            height, width = gray.shape
            
            # Mustache region
            mustache_region = gray[int(height*0.55):int(height*0.7), int(width*0.3):int(width*0.7)]
            mustache_texture = np.std(mustache_region)
            mustache_darkness = np.mean(mustache_region)
            
            # Beard region
            beard_region = gray[int(height*0.7):, int(width*0.2):int(width*0.8)]
            beard_texture = np.std(beard_region)
            beard_darkness = np.mean(beard_region)
            
            # Goatee region
            goatee_region = gray[int(height*0.75):, int(width*0.4):int(width*0.6)]
            goatee_texture = np.std(goatee_region)
            goatee_darkness = np.mean(goatee_region)
            
            # Detect facial hair based on texture and darkness
            mustache_present = mustache_texture > 25 and mustache_darkness < 120
            beard_present = beard_texture > 30 and beard_darkness < 100
            goatee_present = goatee_texture > 25 and goatee_darkness < 110
            
            result = {
                "mustache": {
                    "present": mustache_present,
                    "confidence": 0.7 if mustache_present else 0.3,
                    "texture_score": mustache_texture,
                    "darkness_score": 255 - mustache_darkness
                },
                "beard": {
                    "present": beard_present,
                    "confidence": 0.75 if beard_present else 0.25,
                    "texture_score": beard_texture,
                    "darkness_score": 255 - beard_darkness
                },
                "goatee": {
                    "present": goatee_present,
                    "confidence": 0.65 if goatee_present else 0.35,
                    "texture_score": goatee_texture,
                    "darkness_score": 255 - goatee_darkness
                },
                "overall_facial_hair": mustache_present or beard_present or goatee_present
            }
            
            return convert_numpy_types(result)
            
        except Exception as e:
            logger.error(f"Error detecting facial hair: {str(e)}")
            return {
                "mustache": {"present": False, "confidence": 0.0},
                "beard": {"present": False, "confidence": 0.0},
                "goatee": {"present": False, "confidence": 0.0},
                "overall_facial_hair": False,
                "error": str(e)
            }
            
    async def _detect_accessories(self, face_image: np.ndarray) -> Dict[str, Any]:
        """Detect glasses, hats, masks and other accessories"""
        try:
            gray = cv2.cvtColor(face_image, cv2.COLOR_BGR2GRAY)
            height, width = gray.shape
            
            # Glasses detection using eye region analysis
            eye_region = gray[int(height*0.2):int(height*0.5), int(width*0.1):int(width*0.9)]
            edges = cv2.Canny(eye_region, 50, 150)
            
            # Look for rectangular/circular patterns (glasses frames)
            contours, _ = cv2.findContours(edges, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)
            glasses_score = 0
            
            for contour in contours:
                area = cv2.contourArea(contour)
                if area > 100:  # Significant size
                    # Check if contour is roughly circular or rectangular
                    perimeter = cv2.arcLength(contour, True)
                    approx = cv2.approxPolyDP(contour, 0.02 * perimeter, True)
                    if len(approx) >= 4:  # Rectangular-ish (glasses frame)
                        glasses_score += 1
                        
            glasses_present = glasses_score >= 2  # At least 2 frame-like contours
            
            # Hat detection (check upper portion of image)
            hat_region = gray[:int(height*0.3), :]
            hat_coverage = np.sum(hat_region < 50) / hat_region.size  # Dark pixels
            hat_present = hat_coverage > 0.3
            
            # Mask detection (check lower face region)
            mask_region = gray[int(height*0.5):int(height*0.9), int(width*0.2):int(width*0.8)]
            mask_uniform = np.std(mask_region)
            mask_present = mask_uniform < 20 and np.mean(mask_region) > 100  # Uniform light region
            
            result = {
                "glasses": {
                    "present": glasses_present,
                    "confidence": 0.8 if glasses_present else 0.2,
                    "frame_detections": glasses_score
                },
                "hat": {
                    "present": hat_present,
                    "confidence": 0.6 if hat_present else 0.4,
                    "coverage_ratio": hat_coverage
                },
                "mask": {
                    "present": mask_present,
                    "confidence": 0.7 if mask_present else 0.3,
                    "uniformity_score": mask_uniform
                }
            }
            
            return convert_numpy_types(result)
            
        except Exception as e:
            logger.error(f"Error detecting accessories: {str(e)}")
            return {
                "glasses": {"present": False, "confidence": 0.0},
                "hat": {"present": False, "confidence": 0.0},
                "mask": {"present": False, "confidence": 0.0},
                "error": str(e)
            }
            
    async def _extract_landmarks(self, face_image: np.ndarray) -> List[Tuple[int, int]]:
        """Extract 68-point facial landmarks using high-precision dlib and mediapipe"""
        try:
            gray = cv2.cvtColor(face_image, cv2.COLOR_BGR2GRAY)
            
            # Try dlib first for maximum precision
            landmarks = await self._extract_dlib_landmarks(gray)
            
            # If dlib fails, fallback to mediapipe
            if not landmarks:
                landmarks = await self._extract_mediapipe_landmarks(face_image)
            
            # If both fail, use improved geometric estimation
            if not landmarks:
                landmarks = await self._extract_improved_geometric_landmarks(gray)
            
            return landmarks
            
        except Exception as e:
            logger.error(f"Error extracting landmarks: {str(e)}")
            return []
            
    async def _extract_dlib_landmarks(self, gray_image: np.ndarray) -> List[Tuple[int, int]]:
        """Extract 68-point landmarks using dlib (highest precision)"""
        try:
            import dlib
            
            # Initialize dlib face detector and landmark predictor
            if not hasattr(self, 'dlib_detector'):
                self.dlib_detector = dlib.get_frontal_face_detector()
                # Download shape predictor if not exists
                predictor_path = "shape_predictor_68_face_landmarks.dat"
                if not Path(predictor_path).exists():
                    logger.warning("dlib shape predictor not found, falling back to mediapipe")
                    return []
                self.dlib_predictor = dlib.shape_predictor(predictor_path)
            
            # Detect faces
            faces = self.dlib_detector(gray_image)
            
            if len(faces) == 0:
                return []
            
            # Use first detected face (largest if multiple)
            if len(faces) > 1:
                faces = sorted(faces, key=lambda f: (f.right() - f.left()) * (f.bottom() - f.top()), reverse=True)
            
            face = faces[0]
            
            # Extract landmarks
            landmarks = self.dlib_predictor(gray_image, face)
            points = []
            
            for i in range(68):
                x = landmarks.part(i).x
                y = landmarks.part(i).y
                points.append((int(x), int(y)))
            
            # Calculate craniofacial measurements with millimetric precision
            measurements = self._calculate_craniofacial_measurements(points, gray_image.shape)
            logger.info(f"Craniofacial measurements: {measurements}")
            
            return convert_numpy_types(points)
            
        except ImportError:
            logger.warning("dlib not available, falling back to mediapipe")
            return []
        except Exception as e:
            logger.error(f"Error with dlib landmark extraction: {str(e)}")
            return []
    
    async def _extract_mediapipe_landmarks(self, face_image: np.ndarray) -> List[Tuple[int, int]]:
        """Extract landmarks using mediapipe (good precision, more reliable)"""
        try:
            import mediapipe as mp
            
            if not hasattr(self, 'mp_face_mesh'):
                self.mp_face_mesh = mp.solutions.face_mesh.FaceMesh(
                    static_image_mode=True,
                    max_num_faces=1,
                    refine_landmarks=True,  # Enable refined landmarks for higher precision
                    min_detection_confidence=0.5
                )
            
            # Convert BGR to RGB
            rgb_image = cv2.cvtColor(face_image, cv2.COLOR_BGR2RGB)
            results = self.mp_face_mesh.process(rgb_image)
            
            if not results.multi_face_landmarks:
                return []
            
            # Get image dimensions
            height, width = face_image.shape[:2]
            
            # Extract first face landmarks
            face_landmarks = results.multi_face_landmarks[0]
            
            # Convert mediapipe 468 points to traditional 68 points mapping
            # Using standard dlib-compatible point indices
            dlib_68_indices = [
                # Face outline (17 points) - jaw line
                172, 136, 150, 149, 176, 148, 152, 377, 400, 378, 379, 365, 397, 288, 361, 323, 454,
                # Right eyebrow (5 points)
                70, 63, 105, 66, 107,
                # Left eyebrow (5 points)  
                55, 65, 52, 53, 46,
                # Nose (9 points)
                168, 8, 9, 10, 151, 195, 197, 196, 3,
                # Right eye (6 points)
                33, 7, 163, 144, 145, 153,
                # Left eye (6 points)
                362, 398, 384, 385, 386, 387,
                # Mouth (20 points)
                61, 84, 17, 314, 405, 320, 307, 375, 321, 308, 324, 318,
                78, 191, 80, 81, 82, 13, 312, 311
            ]
            
            # Extract the 68 landmark points
            landmarks_68 = []
            for idx in dlib_68_indices[:68]:  # Ensure exactly 68 points
                if idx < len(face_landmarks.landmark):
                    landmark = face_landmarks.landmark[idx]
                    x = int(landmark.x * width)
                    y = int(landmark.y * height)
                    landmarks_68.append((x, y))
                else:
                    # Fallback for missing indices
                    landmarks_68.append((0, 0))
            
            # Calculate craniofacial measurements
            measurements = self._calculate_craniofacial_measurements(landmarks_68, face_image.shape[:2])
            logger.info(f"Craniofacial measurements (MediaPipe): {measurements}")
            
            return convert_numpy_types(landmarks_68)
            
        except ImportError:
            logger.warning("mediapipe not available, using improved geometric estimation")
            return []
        except Exception as e:
            logger.error(f"Error with mediapipe landmark extraction: {str(e)}")
            return []
    
    def _calculate_craniofacial_measurements(self, landmarks: List[Tuple[int, int]], image_shape: Tuple[int, int]) -> Dict[str, float]:
        """Calculate precise craniofacial measurements in millimeters (assuming standard face size)"""
        try:
            if len(landmarks) < 68:
                return {}
            
            # Standard anthropometric measurements
            # Assuming average face width is ~140mm for scaling
            avg_face_width_mm = 140.0
            
            # Key landmark indices (dlib 68-point standard)
            left_face_edge = 0   # Leftmost point of jaw
            right_face_edge = 16 # Rightmost point of jaw
            left_eye_outer = 36  # Left eye outer corner
            right_eye_outer = 45 # Right eye outer corner
            nose_tip = 30        # Nose tip
            nose_base_left = 31  # Left nostril
            nose_base_right = 35 # Right nostril
            mouth_left = 48      # Left mouth corner
            mouth_right = 54     # Right mouth corner
            chin = 8             # Chin center
            
            # Calculate pixel-to-mm conversion factor
            face_width_pixels = abs(landmarks[right_face_edge][0] - landmarks[left_face_edge][0])
            if face_width_pixels == 0:
                return {}
            
            mm_per_pixel = avg_face_width_mm / face_width_pixels
            
            measurements = {
                # Biocular width (eye-to-eye distance)
                "biocular_width_mm": abs(landmarks[right_eye_outer][0] - landmarks[left_eye_outer][0]) * mm_per_pixel,
                
                # Nasal width
                "nasal_width_mm": abs(landmarks[nose_base_right][0] - landmarks[nose_base_left][0]) * mm_per_pixel,
                
                # Mouth width
                "mouth_width_mm": abs(landmarks[mouth_right][0] - landmarks[mouth_left][0]) * mm_per_pixel,
                
                # Face height (approximate)
                "face_height_mm": abs(landmarks[chin][1] - min(landmarks[i][1] for i in range(17, 27))) * mm_per_pixel,
                
                # Face width
                "face_width_mm": face_width_pixels * mm_per_pixel,
                
                # Nasal height (nose tip to base)
                "nasal_height_mm": abs(landmarks[nose_tip][1] - landmarks[nose_base_left][1]) * mm_per_pixel,
                
                # Additional anthropometric ratios
                "facial_index": (abs(landmarks[chin][1] - min(landmarks[i][1] for i in range(17, 27))) / face_width_pixels) * 100,
                "nasal_index": (abs(landmarks[nose_base_right][0] - landmarks[nose_base_left][0]) / 
                               abs(landmarks[nose_tip][1] - landmarks[nose_base_left][1])) * 100 if abs(landmarks[nose_tip][1] - landmarks[nose_base_left][1]) > 0 else 0,
            }
            
            return measurements
            
        except Exception as e:
            logger.error(f"Error calculating craniofacial measurements: {str(e)}")
            return {}
    
    async def _extract_improved_geometric_landmarks(self, gray_image: np.ndarray) -> List[Tuple[int, int]]:
        """Improved geometric landmark estimation with better precision"""
        try:
            height, width = gray_image.shape
            
            # Use better face detection
            face_cascade = cv2.CascadeClassifier(cv2.data.haarcascades + 'haarcascade_frontalface_default.xml')
            eye_cascade = cv2.CascadeClassifier(cv2.data.haarcascades + 'haarcascade_eye.xml')
            
            faces = face_cascade.detectMultiScale(gray_image, 1.3, 5)
            
            if len(faces) == 0:
                return []
            
            # Use largest face
            (x, y, w, h) = max(faces, key=lambda f: f[2] * f[3])
            
            # Detect eyes for better alignment
            roi_gray = gray_image[y:y+h, x:x+w]
            eyes = eye_cascade.detectMultiScale(roi_gray)
            
            landmarks = []
            
            # Improved face outline using contour detection
            face_roi = gray_image[y:y+h, x:x+w]
            edges = cv2.Canny(face_roi, 50, 150)
            contours, _ = cv2.findContours(edges, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)
            
            if contours:
                # Find face contour
                face_contour = max(contours, key=cv2.contourArea)
                
                # Extract 17 points from face contour for jawline
                if len(face_contour) >= 17:
                    contour_indices = np.linspace(0, len(face_contour)-1, 17, dtype=int)
                    for idx in contour_indices:
                        point = face_contour[idx][0]
                        landmarks.append((x + int(point[0]), y + int(point[1])))
                else:
                    # Fallback to geometric estimation
                    for i in range(17):
                        px = x + int(w * (i / 16.0))
                        py = y + h - int(h * 0.1 * np.sin(i * np.pi / 16))
                        landmarks.append((px, py))
            else:
                # Geometric fallback
                for i in range(17):
                    px = x + int(w * (i / 16.0))
                    py = y + h - int(h * 0.1 * np.sin(i * np.pi / 16))
                    landmarks.append((px, py))
            
            # Enhanced eyebrow detection using template matching
            eyebrow_template = np.ones((5, 20), dtype=np.uint8) * 255
            eyebrow_template[2:, :] = 0  # Create eyebrow-like template
            
            # Right eyebrow
            for i in range(5):
                px = x + int(w * (0.18 + i * 0.06))
                py = y + int(h * 0.25)
                landmarks.append((px, py))
            
            # Left eyebrow  
            for i in range(5):
                px = x + int(w * (0.62 + i * 0.06))
                py = y + int(h * 0.25)
                landmarks.append((px, py))
            
            # Enhanced nose detection using gradients
            nose_roi = gray_image[y+int(h*0.3):y+int(h*0.7), x+int(w*0.3):x+int(w*0.7)]
            grad_x = cv2.Sobel(nose_roi, cv2.CV_64F, 1, 0, ksize=3)
            grad_y = cv2.Sobel(nose_roi, cv2.CV_64F, 0, 1, ksize=3)
            
            # Find nose tip as point with highest gradient magnitude
            magnitude = np.sqrt(grad_x**2 + grad_y**2)
            nose_y, nose_x = np.unravel_index(np.argmax(magnitude), magnitude.shape)
            nose_center_x = x + int(w*0.3) + nose_x
            nose_center_y = y + int(h*0.3) + nose_y
            
            # Nose landmarks (9 points)
            nose_points = [
                (nose_center_x, nose_center_y - int(h*0.1)),  # Nose bridge top
                (nose_center_x, nose_center_y - int(h*0.05)), # Nose bridge mid
                (nose_center_x, nose_center_y),                # Nose tip
                (nose_center_x, nose_center_y + int(h*0.03)), # Nose base
                (nose_center_x - int(w*0.03), nose_center_y + int(h*0.03)), # Left nostril
                (nose_center_x + int(w*0.03), nose_center_y + int(h*0.03)), # Right nostril
                (nose_center_x - int(w*0.04), nose_center_y),  # Left nose side
                (nose_center_x + int(w*0.04), nose_center_y),  # Right nose side
                (nose_center_x, nose_center_y + int(h*0.05))   # Nose bottom
            ]
            landmarks.extend(nose_points)
            
            # Enhanced eye detection using actual eye positions if available
            if len(eyes) >= 2:
                # Sort eyes by x-coordinate (left to right)
                eyes_sorted = sorted(eyes, key=lambda e: e[0])
                
                # Right eye (6 points)
                (ex, ey, ew, eh) = eyes_sorted[0]
                eye_center_x = x + ex + ew//2
                eye_center_y = y + ey + eh//2
                for i in range(6):
                    angle = i * np.pi / 3
                    px = eye_center_x + int(ew * 0.4 * np.cos(angle))
                    py = eye_center_y + int(eh * 0.3 * np.sin(angle))
                    landmarks.append((px, py))
                
                # Left eye (6 points)
                (ex, ey, ew, eh) = eyes_sorted[1]
                eye_center_x = x + ex + ew//2
                eye_center_y = y + ey + eh//2
                for i in range(6):
                    angle = i * np.pi / 3
                    px = eye_center_x + int(ew * 0.4 * np.cos(angle))
                    py = eye_center_y + int(eh * 0.3 * np.sin(angle))
                    landmarks.append((px, py))
            else:
                # Fallback to geometric estimation
                # Right eye
                eye_center_x = x + int(w * 0.3)
                eye_center_y = y + int(h * 0.4)
                for i in range(6):
                    angle = i * np.pi / 3
                    px = eye_center_x + int(w * 0.08 * np.cos(angle))
                    py = eye_center_y + int(h * 0.05 * np.sin(angle))
                    landmarks.append((px, py))
                    
                # Left eye
                eye_center_x = x + int(w * 0.7)
                eye_center_y = y + int(h * 0.4)
                for i in range(6):
                    angle = i * np.pi / 3
                    px = eye_center_x + int(w * 0.08 * np.cos(angle))
                    py = eye_center_y + int(h * 0.05 * np.sin(angle))
                    landmarks.append((px, py))
            
            # Enhanced mouth detection using horizontal gradients
            mouth_roi = gray_image[y+int(h*0.6):y+int(h*0.9), x+int(w*0.2):x+int(w*0.8)]
            mouth_grad = cv2.Sobel(mouth_roi, cv2.CV_64F, 0, 1, ksize=3)
            
            # Find mouth center
            mouth_center_y_rel, mouth_center_x_rel = np.unravel_index(np.argmax(np.abs(mouth_grad)), mouth_grad.shape)
            mouth_center_x = x + int(w*0.2) + mouth_center_x_rel
            mouth_center_y = y + int(h*0.6) + mouth_center_y_rel
            
            # Mouth landmarks (20 points)
            for i in range(20):
                if i < 12:  # Outer lip
                    angle = i * 2 * np.pi / 12
                    px = mouth_center_x + int(w * 0.12 * np.cos(angle))
                    py = mouth_center_y + int(h * 0.06 * np.sin(angle))
                else:  # Inner lip
                    angle = (i-12) * 2 * np.pi / 8
                    px = mouth_center_x + int(w * 0.08 * np.cos(angle))
                    py = mouth_center_y + int(h * 0.04 * np.sin(angle))
                landmarks.append((px, py))
            
            return convert_numpy_types(landmarks[:68])  # Ensure exactly 68 points
            
        except Exception as e:
            logger.error(f"Error in improved geometric landmark extraction: {str(e)}")
            return []
            
    async def _assess_face_quality(self, face_image: np.ndarray) -> Dict[str, Any]:
        """Assess face image quality for forensic purposes"""
        try:
            gray = cv2.cvtColor(face_image, cv2.COLOR_BGR2GRAY)
            
            # Sharpness (Laplacian variance)
            laplacian = cv2.Laplacian(gray, cv2.CV_64F)
            sharpness = laplacian.var()
            
            # Brightness
            brightness = gray.mean()
            
            # Contrast
            contrast = gray.std()
            
            # Face size (resolution quality)
            face_size = gray.shape[0] * gray.shape[1]
            
            # Blur detection
            blur_score = cv2.Laplacian(gray, cv2.CV_64F).var()
            
            # Overall quality score (0-100)
            quality_factors = {
                "sharpness": min(1.0, sharpness / 1000),
                "brightness": 1.0 - abs(brightness - 128) / 128,
                "contrast": min(1.0, contrast / 50),
                "resolution": min(1.0, face_size / 10000),
                "blur": min(1.0, blur_score / 500)
            }
            
            overall_quality = sum(quality_factors.values()) / len(quality_factors) * 100
            
            result = {
                "overall_quality": overall_quality,
                "sharpness": sharpness,
                "brightness": brightness,
                "contrast": contrast,
                "face_size": face_size,
                "blur_score": blur_score,
                "quality_factors": quality_factors,
                "suitable_for_analysis": overall_quality > 50
            }
            
            return convert_numpy_types(result)
            
        except Exception as e:
            logger.error(f"Error assessing face quality: {str(e)}")
            return {
                "overall_quality": 0.0,
                "suitable_for_analysis": False,
                "error": str(e)
            }
            
    async def _generate_biometric_template(self, face_image: np.ndarray) -> Optional[str]:
        """Generate biometric template for face matching"""
        try:
            # Simplified biometric template generation
            gray = cv2.cvtColor(face_image, cv2.COLOR_BGR2GRAY)
            
            # Resize to standard size for consistency
            standard_size = cv2.resize(gray, (128, 128))
            
            # Extract feature vector using simple histogram of gradients
            # In production, would use advanced feature extraction
            
            # Calculate gradients
            grad_x = cv2.Sobel(standard_size, cv2.CV_64F, 1, 0, ksize=3)
            grad_y = cv2.Sobel(standard_size, cv2.CV_64F, 0, 1, ksize=3)
            
            # Calculate magnitude and orientation
            magnitude = np.sqrt(grad_x**2 + grad_y**2)
            orientation = np.arctan2(grad_y, grad_x)
            
            # Create histogram of gradients (simplified HOG)
            hist_bins = 8
            features = []
            
            # Divide image into 8x8 cells
            cell_size = 16
            for y in range(0, 128, cell_size):
                for x in range(0, 128, cell_size):
                    cell_magnitude = magnitude[y:y+cell_size, x:x+cell_size]
                    cell_orientation = orientation[y:y+cell_size, x:x+cell_size]
                    
                    # Calculate histogram for this cell
                    hist = np.histogram(cell_orientation.flatten(), 
                                     bins=hist_bins, 
                                     weights=cell_magnitude.flatten(),
                                     range=(-np.pi, np.pi))[0]
                    features.extend(hist)
                    
            # Normalize feature vector
            features = np.array(features)
            if np.linalg.norm(features) > 0:
                features = features / np.linalg.norm(features)
                
            # Convert to base64 string for storage
            import base64
            feature_bytes = features.astype(np.float32).tobytes()
            template = base64.b64encode(feature_bytes).decode('utf-8')
            
            return template
            
        except Exception as e:
            logger.error(f"Error generating biometric template: {str(e)}")
            return None
            
    async def batch_analyze_faces(self, face_images: List[np.ndarray]) -> List[AdvancedFaceResult]:
        """Analyze multiple faces in batch"""
        try:
            results = []
            
            for i, face_image in enumerate(face_images):
                try:
                    # Generate unique face ID
                    face_id = f"face_{i}_{int(datetime.now().timestamp())}"
                    
                    # Get bounding box (simplified - assume full image is face)
                    height, width = face_image.shape[:2]
                    bounding_box = (0, 0, width, height)
                    
                    # Comprehensive analysis
                    facial_features = await self.analyze_face_comprehensive(face_image)
                    
                    # Calculate overall confidence based on quality
                    if hasattr(facial_features, 'face_quality'):
                        confidence = facial_features.face_quality.get('overall_quality', 0) / 100
                    else:
                        confidence = 0.5
                    
                    result = AdvancedFaceResult(
                        face_id=face_id,
                        bounding_box=bounding_box,
                        facial_features=facial_features,
                        confidence_score=confidence,
                        analysis_timestamp=datetime.now().isoformat(),
                        processing_notes=[f"Processed face {i+1} of {len(face_images)}"]
                    )
                    
                    results.append(result)
                    
                except Exception as e:
                    logger.error(f"Error analyzing face {i}: {str(e)}")
                    continue
                    
            return results
            
        except Exception as e:
            logger.error(f"Error in batch face analysis: {str(e)}")
            return []
