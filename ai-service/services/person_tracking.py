"""
Multiple Person Tracking System for Forensic Surveillance Analysis
Provides person re-identification, crowd analysis, and behavior detection
"""

import cv2
import numpy as np
import asyncio
import logging
from typing import Dict, List, Tuple, Optional, Any
from dataclasses import dataclass
from pathlib import Path
import json
from datetime import datetime, timedelta
import uuid

# Setup logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

@dataclass
class PersonTrack:
    """Individual person tracking data"""
    track_id: str
    first_seen: datetime
    last_seen: datetime
    bounding_boxes: List[Tuple[int, int, int, int, float]]  # x,y,w,h,timestamp
    face_images: List[np.ndarray]
    movement_path: List[Tuple[int, int, float]]  # x,y,timestamp
    biometric_template: Optional[str]
    confidence_score: float
    demographics: Dict[str, Any]
    behavior_flags: List[str]
    camera_ids: List[str]

@dataclass
class SuspiciousBehavior:
    """Suspicious behavior detection result"""
    behavior_type: str
    confidence: float
    timestamp: datetime
    location: Tuple[int, int]
    track_id: str
    description: str
    severity: str  # low, medium, high, critical

@dataclass
class CrowdAnalysis:
    """Crowd analysis metrics"""
    total_people: int
    crowd_density: float
    movement_patterns: Dict[str, Any]
    congestion_points: List[Tuple[int, int]]
    flow_direction: Dict[str, float]
    anomalous_movements: List[Dict[str, Any]]
    timestamp: datetime

class MultiplePersonTrackingService:
    """Advanced person tracking and behavior analysis system"""
    
    def __init__(self):
        self.active_tracks = {}  # track_id -> PersonTrack
        self.behavior_detector = BehaviorDetector()
        self.person_detector = cv2.HOGDescriptor()
        self.person_detector.setSVMDetector(cv2.HOGDescriptor_getDefaultPeopleDetector())
        self.tracking_history = []
        logger.info("Multiple Person Tracking Service initialized")
        
    async def track_people_in_video(self, video_path: str, camera_id: str = "default") -> Dict[str, Any]:
        """Track multiple people throughout video with re-identification"""
        try:
            cap = cv2.VideoCapture(video_path)
            if not cap.isOpened():
                raise ValueError(f"Cannot open video: {video_path}")
                
            fps = cap.get(cv2.CAP_PROP_FPS)
            frame_count = int(cap.get(cv2.CAP_PROP_FRAME_COUNT))
            
            tracks = {}
            frame_number = 0
            crowd_data = []
            behaviors = []
            
            logger.info(f"Processing {frame_count} frames for person tracking...")
            
            while True:
                ret, frame = cap.read()
                if not ret:
                    break
                    
                timestamp = frame_number / fps
                current_time = datetime.now() + timedelta(seconds=timestamp)
                
                # Detect people in current frame
                people_detections = await self._detect_people(frame)
                
                # Update tracks with new detections
                tracks = await self._update_tracks(tracks, people_detections, current_time, camera_id)
                
                # Analyze crowd behavior
                if frame_number % 30 == 0:  # Every second
                    crowd_analysis = await self._analyze_crowd(frame, people_detections, current_time)
                    crowd_data.append(crowd_analysis)
                
                # Detect suspicious behaviors
                frame_behaviors = await self._detect_behaviors(frame, tracks, current_time)
                behaviors.extend(frame_behaviors)
                
                frame_number += 1
                
                # Progress logging
                if frame_number % 100 == 0:
                    progress = (frame_number / frame_count) * 100
                    logger.info(f"Tracking progress: {progress:.1f}%")
                    
            cap.release()
            
            # Generate final analysis
            tracking_summary = await self._generate_tracking_summary(tracks, crowd_data, behaviors)
            
            return {
                "success": True,
                "video_path": video_path,
                "camera_id": camera_id,
                "processing_stats": {
                    "total_frames": frame_count,
                    "fps": fps,
                    "duration_seconds": frame_count / fps,
                    "unique_people": len(tracks),
                    "crowd_analyses": len(crowd_data),
                    "behaviors_detected": len(behaviors)
                },
                "tracks": {track_id: self._serialize_track(track) for track_id, track in tracks.items()},
                "crowd_analysis": [self._serialize_crowd(c) for c in crowd_data],
                "suspicious_behaviors": [self._serialize_behavior(b) for b in behaviors],
                "summary": tracking_summary
            }
            
        except Exception as e:
            logger.error(f"Error tracking people in video: {str(e)}")
            raise
            
    async def _detect_people(self, frame: np.ndarray) -> List[Dict[str, Any]]:
        """Detect people in frame using HOG detector"""
        try:
            # Detect people
            people, weights = self.person_detector.detectMultiScale(
                frame, winStride=(8, 8), padding=(32, 32), scale=1.05
            )
            
            detections = []
            for i, (x, y, w, h) in enumerate(people):
                confidence = float(weights[i]) if i < len(weights) else 0.5
                
                # Extract person image
                person_img = frame[y:y+h, x:x+w]
                
                # Generate simple biometric template
                template = await self._generate_person_template(person_img)
                
                detection = {
                    "bbox": (x, y, w, h),
                    "confidence": confidence,
                    "person_image": person_img,
                    "template": template
                }
                detections.append(detection)
                
            return detections
            
        except Exception as e:
            logger.error(f"Error detecting people: {str(e)}")
            return []
            
    async def _generate_person_template(self, person_img: np.ndarray) -> str:
        """Generate biometric template for person re-identification"""
        try:
            if person_img.size == 0:
                return ""
                
            # Resize to standard size
            resized = cv2.resize(person_img, (64, 128))
            gray = cv2.cvtColor(resized, cv2.COLOR_BGR2GRAY)
            
            # Extract HOG features
            hog = cv2.HOGDescriptor((64, 128), (16, 16), (8, 8), (8, 8), 9)
            features = hog.compute(gray)
            
            # Convert to base64 string
            import base64
            feature_bytes = features.astype(np.float32).tobytes()
            template = base64.b64encode(feature_bytes).decode('utf-8')
            
            return template
            
        except Exception as e:
            logger.error(f"Error generating person template: {str(e)}")
            return ""
            
    async def _update_tracks(self, tracks: Dict[str, PersonTrack], detections: List[Dict], 
                           current_time: datetime, camera_id: str) -> Dict[str, PersonTrack]:
        """Update person tracks with new detections"""
        try:
            # Match detections to existing tracks
            matched_tracks = set()
            
            for detection in detections:
                bbox = detection["bbox"]
                template = detection["template"]
                person_img = detection["person_image"]
                
                best_match = None
                best_similarity = 0.0
                
                # Find best matching track
                for track_id, track in tracks.items():
                    if track_id in matched_tracks:
                        continue
                        
                    similarity = await self._calculate_similarity(template, track.biometric_template)
                    
                    # Also consider spatial proximity
                    if track.bounding_boxes:
                        last_bbox = track.bounding_boxes[-1][:4]
                        spatial_distance = self._calculate_bbox_distance(bbox, last_bbox)
                        # Combine template similarity with spatial proximity
                        combined_score = similarity * 0.7 + (1.0 - min(spatial_distance/200, 1.0)) * 0.3
                    else:
                        combined_score = similarity
                    
                    if combined_score > best_similarity and combined_score > 0.5:
                        best_similarity = combined_score
                        best_match = track_id
                
                if best_match:
                    # Update existing track
                    track = tracks[best_match]
                    track.last_seen = current_time
                    track.bounding_boxes.append((*bbox, current_time.timestamp()))
                    track.face_images.append(person_img)
                    track.movement_path.append((bbox[0] + bbox[2]//2, bbox[1] + bbox[3]//2, current_time.timestamp()))
                    if camera_id not in track.camera_ids:
                        track.camera_ids.append(camera_id)
                    matched_tracks.add(best_match)
                else:
                    # Create new track
                    track_id = str(uuid.uuid4())
                    new_track = PersonTrack(
                        track_id=track_id,
                        first_seen=current_time,
                        last_seen=current_time,
                        bounding_boxes=[(*bbox, current_time.timestamp())],
                        face_images=[person_img],
                        movement_path=[(bbox[0] + bbox[2]//2, bbox[1] + bbox[3]//2, current_time.timestamp())],
                        biometric_template=template,
                        confidence_score=detection["confidence"],
                        demographics={},
                        behavior_flags=[],
                        camera_ids=[camera_id]
                    )
                    tracks[track_id] = new_track
                    
            return tracks
            
        except Exception as e:
            logger.error(f"Error updating tracks: {str(e)}")
            return tracks
            
    async def _calculate_similarity(self, template1: str, template2: str) -> float:
        """Calculate similarity between two biometric templates"""
        try:
            if not template1 or not template2:
                return 0.0
                
            import base64
            
            # Decode templates
            bytes1 = base64.b64decode(template1)
            bytes2 = base64.b64decode(template2)
            
            # Convert to numpy arrays
            features1 = np.frombuffer(bytes1, dtype=np.float32)
            features2 = np.frombuffer(bytes2, dtype=np.float32)
            
            if len(features1) != len(features2):
                return 0.0
                
            # Calculate cosine similarity
            dot_product = np.dot(features1, features2)
            norm1 = np.linalg.norm(features1)
            norm2 = np.linalg.norm(features2)
            
            if norm1 == 0 or norm2 == 0:
                return 0.0
                
            similarity = dot_product / (norm1 * norm2)
            return max(0.0, min(1.0, similarity))
            
        except Exception as e:
            logger.error(f"Error calculating similarity: {str(e)}")
            return 0.0
            
    def _calculate_bbox_distance(self, bbox1: Tuple[int, int, int, int], 
                               bbox2: Tuple[int, int, int, int]) -> float:
        """Calculate distance between two bounding boxes"""
        center1 = (bbox1[0] + bbox1[2]//2, bbox1[1] + bbox1[3]//2)
        center2 = (bbox2[0] + bbox2[2]//2, bbox2[1] + bbox2[3]//2)
        
        distance = np.sqrt((center1[0] - center2[0])**2 + (center1[1] - center2[1])**2)
        return distance
        
    async def _analyze_crowd(self, frame: np.ndarray, detections: List[Dict], 
                           current_time: datetime) -> CrowdAnalysis:
        """Analyze crowd behavior and density"""
        try:
            total_people = len(detections)
            frame_area = frame.shape[0] * frame.shape[1]
            crowd_density = total_people / (frame_area / 10000)  # People per 100x100 pixel area
            
            # Analyze movement patterns
            centers = []
            for detection in detections:
                bbox = detection["bbox"]
                center_x = bbox[0] + bbox[2] // 2
                center_y = bbox[1] + bbox[3] // 2
                centers.append((center_x, center_y))
            
            # Find congestion points (areas with high person density)
            congestion_points = await self._find_congestion_points(centers, frame.shape)
            
            # Analyze flow direction
            flow_direction = await self._analyze_flow_direction(centers)
            
            return CrowdAnalysis(
                total_people=total_people,
                crowd_density=crowd_density,
                movement_patterns={"centers": centers},
                congestion_points=congestion_points,
                flow_direction=flow_direction,
                anomalous_movements=[],
                timestamp=current_time
            )
            
        except Exception as e:
            logger.error(f"Error analyzing crowd: {str(e)}")
            return CrowdAnalysis(
                total_people=0, crowd_density=0.0, movement_patterns={},
                congestion_points=[], flow_direction={}, anomalous_movements=[],
                timestamp=current_time
            )
            
    async def _find_congestion_points(self, centers: List[Tuple[int, int]], 
                                    frame_shape: Tuple[int, int]) -> List[Tuple[int, int]]:
        """Find areas with high person density"""
        try:
            if len(centers) < 3:
                return []
                
            # Create density map
            height, width = frame_shape[:2]
            grid_size = 50
            grid_h = height // grid_size
            grid_w = width // grid_size
            
            density_map = np.zeros((grid_h, grid_w))
            
            for center_x, center_y in centers:
                grid_x = min(center_x // grid_size, grid_w - 1)
                grid_y = min(center_y // grid_size, grid_h - 1)
                density_map[grid_y, grid_x] += 1
                
            # Find high density areas
            threshold = np.mean(density_map) + np.std(density_map)
            congestion_points = []
            
            for y in range(grid_h):
                for x in range(grid_w):
                    if density_map[y, x] > threshold:
                        actual_x = x * grid_size + grid_size // 2
                        actual_y = y * grid_size + grid_size // 2
                        congestion_points.append((actual_x, actual_y))
                        
            return congestion_points
            
        except Exception as e:
            logger.error(f"Error finding congestion points: {str(e)}")
            return []
            
    async def _analyze_flow_direction(self, centers: List[Tuple[int, int]]) -> Dict[str, float]:
        """Analyze general flow direction of crowd"""
        try:
            if len(centers) < 2:
                return {"north": 0.0, "south": 0.0, "east": 0.0, "west": 0.0}
                
            # Simple flow analysis based on position distribution
            x_coords = [c[0] for c in centers]
            y_coords = [c[1] for c in centers]
            
            # Calculate center of mass
            center_x = np.mean(x_coords)
            center_y = np.mean(y_coords)
            
            # Analyze distribution relative to center
            north_bias = sum(1 for y in y_coords if y < center_y) / len(y_coords)
            south_bias = sum(1 for y in y_coords if y > center_y) / len(y_coords)
            west_bias = sum(1 for x in x_coords if x < center_x) / len(x_coords)
            east_bias = sum(1 for x in x_coords if x > center_x) / len(x_coords)
            
            return {
                "north": north_bias,
                "south": south_bias,
                "east": east_bias,
                "west": west_bias
            }
            
        except Exception as e:
            logger.error(f"Error analyzing flow direction: {str(e)}")
            return {"north": 0.0, "south": 0.0, "east": 0.0, "west": 0.0}
            
    async def _detect_behaviors(self, frame: np.ndarray, tracks: Dict[str, PersonTrack], 
                              current_time: datetime) -> List[SuspiciousBehavior]:
        """Detect suspicious behaviors in current frame"""
        try:
            behaviors = []
            
            for track_id, track in tracks.items():
                if len(track.movement_path) < 10:  # Need minimum movement history
                    continue
                    
                # Analyze movement patterns
                recent_path = track.movement_path[-10:]
                
                # Detect loitering (staying in same area)
                if await self._detect_loitering(recent_path):
                    behavior = SuspiciousBehavior(
                        behavior_type="loitering",
                        confidence=0.8,
                        timestamp=current_time,
                        location=recent_path[-1][:2],
                        track_id=track_id,
                        description="Person staying in same area for extended time",
                        severity="medium"
                    )
                    behaviors.append(behavior)
                
                # Detect erratic movement
                if await self._detect_erratic_movement(recent_path):
                    behavior = SuspiciousBehavior(
                        behavior_type="erratic_movement",
                        confidence=0.7,
                        timestamp=current_time,
                        location=recent_path[-1][:2],
                        track_id=track_id,
                        description="Person showing erratic movement patterns",
                        severity="low"
                    )
                    behaviors.append(behavior)
                    
            return behaviors
            
        except Exception as e:
            logger.error(f"Error detecting behaviors: {str(e)}")
            return []
            
    async def _detect_loitering(self, movement_path: List[Tuple[int, int, float]]) -> bool:
        """Detect if person is loitering in area"""
        try:
            if len(movement_path) < 5:
                return False
                
            # Calculate movement distances
            distances = []
            for i in range(1, len(movement_path)):
                x1, y1, _ = movement_path[i-1]
                x2, y2, _ = movement_path[i]
                distance = np.sqrt((x2-x1)**2 + (y2-y1)**2)
                distances.append(distance)
                
            # If average movement is very small, likely loitering
            avg_distance = np.mean(distances)
            return avg_distance < 20  # pixels
            
        except Exception as e:
            logger.error(f"Error detecting loitering: {str(e)}")
            return False
            
    async def _detect_erratic_movement(self, movement_path: List[Tuple[int, int, float]]) -> bool:
        """Detect erratic movement patterns"""
        try:
            if len(movement_path) < 5:
                return False
                
            # Calculate direction changes
            direction_changes = 0
            
            for i in range(2, len(movement_path)):
                x1, y1, _ = movement_path[i-2]
                x2, y2, _ = movement_path[i-1]
                x3, y3, _ = movement_path[i]
                
                # Calculate vectors
                v1 = (x2-x1, y2-y1)
                v2 = (x3-x2, y3-y2)
                
                # Calculate angle between vectors
                if np.linalg.norm(v1) > 0 and np.linalg.norm(v2) > 0:
                    cos_angle = np.dot(v1, v2) / (np.linalg.norm(v1) * np.linalg.norm(v2))
                    angle = np.arccos(np.clip(cos_angle, -1, 1))
                    
                    # If angle > 90 degrees, it's a significant direction change
                    if angle > np.pi/2:
                        direction_changes += 1
                        
            # If too many direction changes, movement is erratic
            return direction_changes > len(movement_path) * 0.6
            
        except Exception as e:
            logger.error(f"Error detecting erratic movement: {str(e)}")
            return False
            
    async def _generate_tracking_summary(self, tracks: Dict[str, PersonTrack], 
                                       crowd_data: List[CrowdAnalysis],
                                       behaviors: List[SuspiciousBehavior]) -> Dict[str, Any]:
        """Generate comprehensive tracking summary"""
        try:
            total_tracks = len(tracks)
            avg_track_duration = 0
            if tracks:
                durations = [(track.last_seen - track.first_seen).total_seconds() for track in tracks.values()]
                avg_track_duration = np.mean(durations)
                
            max_crowd_size = max((c.total_people for c in crowd_data), default=0)
            avg_crowd_density = np.mean([c.crowd_density for c in crowd_data]) if crowd_data else 0
            
            behavior_summary = {}
            for behavior in behaviors:
                behavior_type = behavior.behavior_type
                if behavior_type not in behavior_summary:
                    behavior_summary[behavior_type] = 0
                behavior_summary[behavior_type] += 1
                
            return {
                "tracking_stats": {
                    "total_unique_people": total_tracks,
                    "average_track_duration_seconds": avg_track_duration,
                    "longest_track_duration": max(durations) if tracks else 0,
                    "shortest_track_duration": min(durations) if tracks else 0
                },
                "crowd_stats": {
                    "max_crowd_size": max_crowd_size,
                    "average_crowd_density": avg_crowd_density,
                    "total_crowd_analyses": len(crowd_data)
                },
                "behavior_stats": {
                    "total_suspicious_behaviors": len(behaviors),
                    "behavior_breakdown": behavior_summary,
                    "high_severity_count": sum(1 for b in behaviors if b.severity == "high"),
                    "medium_severity_count": sum(1 for b in behaviors if b.severity == "medium")
                }
            }
            
        except Exception as e:
            logger.error(f"Error generating tracking summary: {str(e)}")
            return {}
            
    def _serialize_track(self, track: PersonTrack) -> Dict[str, Any]:
        """Serialize PersonTrack for JSON response"""
        return {
            "track_id": track.track_id,
            "first_seen": track.first_seen.isoformat(),
            "last_seen": track.last_seen.isoformat(),
            "duration_seconds": (track.last_seen - track.first_seen).total_seconds(),
            "total_detections": len(track.bounding_boxes),
            "confidence_score": track.confidence_score,
            "behavior_flags": track.behavior_flags,
            "camera_ids": track.camera_ids,
            "has_biometric_template": bool(track.biometric_template)
        }
        
    def _serialize_crowd(self, crowd: CrowdAnalysis) -> Dict[str, Any]:
        """Serialize CrowdAnalysis for JSON response"""
        return {
            "timestamp": crowd.timestamp.isoformat(),
            "total_people": crowd.total_people,
            "crowd_density": crowd.crowd_density,
            "congestion_points": crowd.congestion_points,
            "flow_direction": crowd.flow_direction,
            "anomalous_movements_count": len(crowd.anomalous_movements)
        }
        
    def _serialize_behavior(self, behavior: SuspiciousBehavior) -> Dict[str, Any]:
        """Serialize SuspiciousBehavior for JSON response"""
        return {
            "behavior_type": behavior.behavior_type,
            "confidence": behavior.confidence,
            "timestamp": behavior.timestamp.isoformat(),
            "location": behavior.location,
            "track_id": behavior.track_id,
            "description": behavior.description,
            "severity": behavior.severity
        }

class BehaviorDetector:
    """Specialized behavior detection algorithms"""
    
    def __init__(self):
        pass
