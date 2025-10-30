from pydantic import BaseModel
from typing import List, Optional, Dict, Any
from enum import Enum

class DecisionType(str, Enum):
    MATCH = "match"
    NO_MATCH = "no_match"
    UNCERTAIN = "uncertain"

class ConfidenceLevel(str, Enum):
    VERY_HIGH = "very_high"
    HIGH = "high"
    MEDIUM = "medium"
    LOW = "low"
    VERY_LOW = "very_low"

class BoundingBox(BaseModel):
    x: float
    y: float
    width: float
    height: float

class FaceLandmark(BaseModel):
    x: float
    y: float

class FaceQuality(BaseModel):
    score: float
    blur: float
    brightness: float

class DetectedFace(BaseModel):
    bbox: BoundingBox
    landmarks: List[FaceLandmark]
    quality: FaceQuality
    embedding: Optional[List[float]] = None

class AnalysisResult(BaseModel):
    faces: List[DetectedFace]
    confidence: ConfidenceLevel
    decision: DecisionType

class FaceDetectionResult(BaseModel):
    bbox: List[float]
    kps: Optional[List[List[float]]] = None
    det_score: float
    embedding: Optional[List[float]] = None

class FaceComparisonResult(BaseModel):
    similarity_score: float
    is_same_person: bool

class ConfidenceInterval(BaseModel):
    """Statistical confidence interval for similarity scores"""
    lower_bound: float
    upper_bound: float
    confidence_level: float  # e.g., 0.95 for 95% confidence

class ScientificBasis(BaseModel):
    """Scientific basis and validation for the analysis"""
    algorithm: str
    paper_reference: str
    doi: Optional[str] = None
    statistical_significance: str
    validation_dataset: str
    accuracy_metrics: Dict[str, float]

class BiometricMetrics(BaseModel):
    """Biometric system performance metrics"""
    false_reject_rate: float  # FRR - Type I error
    false_accept_rate: float  # FAR - Type II error
    equal_error_rate: Optional[float] = None  # EER - where FRR = FAR
    decidability_index: Optional[float] = None  # d-prime metric
    roc_auc: Optional[float] = None  # Area under ROC curve

class FaceQualityAdvanced(BaseModel):
    """Advanced face quality metrics"""
    blur_score: float
    brightness_score: float
    resolution_score: float
    pose_score: float
    overall_score: float
    quality_level: str  # POOR, FAIR, GOOD, EXCELLENT

class ComparisonResult(BaseModel):
    """Comprehensive face comparison result with forensic-grade metrics"""
    comparison_id: str
    image1_id: str
    image2_id: str
    match_score: float
    threshold: float
    decision: DecisionType
    confidence_level: ConfidenceLevel
    
    # Advanced similarity metrics
    embedding_distance: float
    cosine_similarity: float
    
    # Statistical analysis
    confidence_interval: Optional[ConfidenceInterval] = None
    biometric_metrics: Optional[BiometricMetrics] = None
    
    # Quality assessment
    face1_quality: Optional[FaceQualityAdvanced] = None
    face2_quality: Optional[FaceQualityAdvanced] = None
    
    # Technical details
    model_version: str
    processing_time_ms: float
    
    # Scientific validation
    scientific_basis: Optional[ScientificBasis] = None
    
    # Additional metadata
    metadata: Optional[Dict[str, Any]] = None

class BatchComparisonResult(BaseModel):
    """Results from batch face comparison operations"""
    batch_id: str
    total_comparisons: int
    successful_comparisons: int
    failed_comparisons: int
    average_processing_time_ms: float
    results: List[ComparisonResult]
    summary_statistics: Optional[Dict[str, Any]] = None

class QualityMetrics(BaseModel):
    overall_score: float
    blur_score: float
    brightness_score: float
    contrast_score: float
    sharpness_score: float
    noise_score: float
    brightness: float
    contrast: float

class FrameResult(BaseModel):
    frame_index: int
    quality_metrics: QualityMetrics
    faces_detected: int
    faces: List[FaceDetectionResult]

class VideoInfo(BaseModel):
    fps: float
    frame_count: int
    width: int
    height: int
    duration: float
    file_size: int
    resolution: str
    aspect_ratio: float

class VideoAnalysisResult(BaseModel):
    video_info: VideoInfo
    frames_processed: int
    frames: List[FrameResult]

class BatchProcessingResult(BaseModel):
    total_items: int
    successful_items: int
    failed_items: int
    results: List[Dict[str, Any]]

class VideoFrame(BaseModel):
    frame_number: int
    timestamp: float
    faces: List[DetectedFace]

class VideoProcessingResult(BaseModel):
    total_frames: int
    processed_frames: int
    frames: List[VideoFrame]

class ProcessingStats(BaseModel):
    max_workers: int
    active_tasks: int
    executor_stats: Dict[str, Any]
