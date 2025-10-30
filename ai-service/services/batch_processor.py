import asyncio
import json
from typing import List, Dict, Any, Optional
from concurrent.futures import ThreadPoolExecutor
import time
from .face_detection_mock import FaceDetectionService
from .video_processor import VideoProcessorService
import numpy as np
from PIL import Image
import io
import cv2

class BatchProcessor:
    def __init__(self, max_workers: int = 4):
        self.max_workers = max_workers
        self.face_detector = FaceDetectionService()
        self.video_processor = VideoProcessorService()
        self.executor = ThreadPoolExecutor(max_workers=max_workers)
    
    async def process_batch_images(self, image_data_list: List[Dict[str, Any]]) -> List[Dict[str, Any]]:
        """
        Processes a batch of images for face detection and analysis.
        :param image_data_list: List of dictionaries containing image data and metadata.
        :return: List of processing results.
        """
        tasks = []
        for image_data in image_data_list:
            task = asyncio.create_task(self._process_single_image(image_data))
            tasks.append(task)
        
        results = await asyncio.gather(*tasks, return_exceptions=True)
        
        # Handle exceptions
        processed_results = []
        for i, result in enumerate(results):
            if isinstance(result, Exception):
                processed_results.append({
                    'image_id': image_data_list[i].get('id', f'image_{i}'),
                    'success': False,
                    'error': str(result),
                    'processing_time': 0
                })
            else:
                processed_results.append(result)
        
        return processed_results
    
    async def _process_single_image(self, image_data: Dict[str, Any]) -> Dict[str, Any]:
        """
        Processes a single image for face detection.
        :param image_data: Dictionary containing image data and metadata.
        :return: Processing result.
        """
        start_time = time.time()
        
        try:
            # Decode image from base64 or bytes
            if 'image_data' in image_data:
                if isinstance(image_data['image_data'], str):
                    # Assume it's base64 encoded
                    import base64
                    image_bytes = base64.b64decode(image_data['image_data'])
                else:
                    image_bytes = image_data['image_data']
            else:
                raise ValueError("No image data provided")
            
            # Convert to OpenCV format
            image = Image.open(io.BytesIO(image_bytes)).convert("RGB")
            image_np = np.array(image)
            image_np = cv2.cvtColor(image_np, cv2.COLOR_RGB2BGR)
            
            # Detect faces
            faces = await self.face_detector.detect_faces(image_np)
            
            # Process results
            face_results = []
            for face in faces:
                # Handle bbox safely
                bbox = face.bbox if isinstance(face.bbox, list) else [0, 0, 0, 0]
                
                # Handle kps safely
                kps = None
                if face.kps is not None:
                    if isinstance(face.kps, list):
                        kps = face.kps
                        
                # Handle embedding safely
                embedding = None
                if face.embedding is not None:
                    if isinstance(face.embedding, list):
                        embedding = face.embedding
                
                face_result = {
                    'bbox': bbox,
                    'kps': kps,
                    'det_score': float(face.det_score) if hasattr(face, 'det_score') else 0.0,
                    'embedding': embedding
                }
                face_results.append(face_result)
            
            processing_time = time.time() - start_time
            
            return {
                'image_id': image_data.get('id', 'unknown'),
                'success': True,
                'faces_detected': len(faces),
                'faces': face_results,
                'processing_time': processing_time,
                'image_metadata': {
                    'width': image_np.shape[1],
                    'height': image_np.shape[0],
                    'channels': image_np.shape[2] if len(image_np.shape) > 2 else 1
                }
            }
            
        except Exception as e:
            processing_time = time.time() - start_time
            return {
                'image_id': image_data.get('id', 'unknown'),
                'success': False,
                'error': str(e),
                'processing_time': processing_time
            }
    
    async def process_batch_videos(self, video_data_list: List[Dict[str, Any]]) -> List[Dict[str, Any]]:
        """
        Processes a batch of videos for frame extraction and face detection.
        :param video_data_list: List of dictionaries containing video data and metadata.
        :return: List of processing results.
        """
        tasks = []
        for video_data in video_data_list:
            task = asyncio.create_task(self._process_single_video(video_data))
            tasks.append(task)
        
        results = await asyncio.gather(*tasks, return_exceptions=True)
        
        # Handle exceptions
        processed_results = []
        for i, result in enumerate(results):
            if isinstance(result, Exception):
                processed_results.append({
                    'video_id': video_data_list[i].get('id', f'video_{i}'),
                    'success': False,
                    'error': str(result),
                    'processing_time': 0
                })
            else:
                processed_results.append(result)
        
        return processed_results
    
    async def _process_single_video(self, video_data: Dict[str, Any]) -> Dict[str, Any]:
        """
        Processes a single video for frame extraction and face detection.
        :param video_data: Dictionary containing video data and metadata.
        :return: Processing result.
        """
        start_time = time.time()
        
        try:
            video_path = video_data.get('video_path')
            if not video_path:
                raise ValueError("No video path provided")
            
            # Get video information
            video_info = self.video_processor.get_video_info(video_path)
            
            # Extract best frames
            num_frames = video_data.get('num_frames', 10)
            frames_per_second = video_data.get('frames_per_second', 1)
            
            best_frames = self.video_processor.extract_best_frames(
                video_path, num_frames, frames_per_second
            )
            
            # Process frames for face detection
            frame_results = []
            for frame_data in best_frames:
                # Decode frame from hex
                frame_bytes = bytes.fromhex(frame_data['frame_data'])
                
                # Convert to OpenCV format
                nparr = np.frombuffer(frame_bytes, np.uint8)
                frame = cv2.imdecode(nparr, cv2.IMREAD_COLOR)
                
                # Detect faces in frame
                faces = await self.face_detector.detect_faces(frame)
                
                # Process face results
                face_results = []
                for face in faces:
                    # Handle bbox safely
                    bbox = face.bbox if isinstance(face.bbox, list) else [0, 0, 0, 0]
                    
                    # Handle kps safely
                    kps = None
                    if face.kps is not None:
                        if isinstance(face.kps, list):
                            kps = face.kps
                            
                    # Handle embedding safely
                    embedding = None
                    if face.embedding is not None:
                        if isinstance(face.embedding, list):
                            embedding = face.embedding
                    
                    face_result = {
                        'bbox': bbox,
                        'kps': kps,
                        'det_score': float(face.det_score) if hasattr(face, 'det_score') else 0.0,
                        'embedding': embedding
                    }
                    face_results.append(face_result)
                
                frame_results.append({
                    'frame_index': frame_data['frame_index'],
                    'quality_metrics': frame_data['quality_metrics'],
                    'faces_detected': len(faces),
                    'faces': face_results
                })
            
            processing_time = time.time() - start_time
            
            return {
                'video_id': video_data.get('id', 'unknown'),
                'success': True,
                'video_info': video_info,
                'frames_processed': len(frame_results),
                'frames': frame_results,
                'processing_time': processing_time
            }
            
        except Exception as e:
            processing_time = time.time() - start_time
            return {
                'video_id': video_data.get('id', 'unknown'),
                'success': False,
                'error': str(e),
                'processing_time': processing_time
            }
    
    async def compare_faces_batch(self, comparison_pairs: List[Dict[str, Any]]) -> List[Dict[str, Any]]:
        """
        Compares faces in batch mode.
        :param comparison_pairs: List of face comparison pairs.
        :return: List of comparison results.
        """
        tasks = []
        for pair in comparison_pairs:
            task = asyncio.create_task(self._compare_face_pair(pair))
            tasks.append(task)
        
        results = await asyncio.gather(*tasks, return_exceptions=True)
        
        # Handle exceptions
        processed_results = []
        for i, result in enumerate(results):
            if isinstance(result, Exception):
                processed_results.append({
                    'pair_id': comparison_pairs[i].get('id', f'pair_{i}'),
                    'success': False,
                    'error': str(result),
                    'processing_time': 0
                })
            else:
                processed_results.append(result)
        
        return processed_results
    
    async def _compare_face_pair(self, pair_data: Dict[str, Any]) -> Dict[str, Any]:
        """
        Compares a pair of faces.
        :param pair_data: Dictionary containing face pair data.
        :return: Comparison result.
        """
        start_time = time.time()
        
        try:
            embedding1 = np.array(pair_data['embedding1'])
            embedding2 = np.array(pair_data['embedding2'])
            
            # Calculate similarity
            similarity_score = self.face_detector.calculate_similarity(embedding1, embedding2)
            
            # Determine if same person (threshold can be configurable)
            threshold = pair_data.get('threshold', 0.6)
            is_same_person = similarity_score > threshold
            
            processing_time = time.time() - start_time
            
            return {
                'pair_id': pair_data.get('id', 'unknown'),
                'success': True,
                'similarity_score': float(similarity_score),
                'is_same_person': is_same_person,
                'threshold': threshold,
                'processing_time': processing_time
            }
            
        except Exception as e:
            processing_time = time.time() - start_time
            return {
                'pair_id': pair_data.get('id', 'unknown'),
                'success': False,
                'error': str(e),
                'processing_time': processing_time
            }
    
    def get_processing_stats(self) -> Dict[str, Any]:
        """
        Returns current processing statistics.
        :return: Dictionary with processing stats.
        """
        return {
            'max_workers': self.max_workers,
            'active_tasks': len([task for task in asyncio.all_tasks() if not task.done()]),
            'executor_stats': {
                'active_threads': self.executor._threads,
                'pending_tasks': len(self.executor._work_queue)
            }
        }
