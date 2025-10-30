#!/usr/bin/env python3
import requests
import json
import base64
import io
from pathlib import Path

def create_simple_test_image():
    """Create a simple test image as base64"""
    
    # 1x1 pixel beyaz PNG görüntüsü (minimum valid PNG)
    # PNG header + IHDR chunk + IDAT chunk + IEND chunk
    png_data = bytes.fromhex(
        '89504e470d0a1a0a'  # PNG signature
        '0000000d'          # IHDR length
        '49484452'          # IHDR
        '00000001'          # width: 1
        '00000001'          # height: 1  
        '08'                # bit depth: 8
        '02'                # color type: RGB
        '00'                # compression: 0
        '00'                # filter: 0
        '00'                # interlace: 0
        '90773852'          # IHDR CRC
        '0000000c'          # IDAT length
        '49444154'          # IDAT
        '789c626001000000ffff0300060005'  # compressed RGB data (white pixel)
        '57bfabc4'          # IDAT CRC
        '00000000'          # IEND length
        '49454e44'          # IEND
        'ae426082'          # IEND CRC
    )
    
    return base64.b64encode(png_data).decode()

def test_identical_comparison():
    """Test comparing identical images"""
    
    print("Creating test image...")
    test_image_base64 = create_simple_test_image()
    
    # AI servisine aynı görüntüyü kendisiyle karşılaştır
    url = "http://localhost:8000/compare-faces"
    payload = {
        "image1": test_image_base64,
        "image2": test_image_base64
    }
    
    print("Sending request to AI service...")
    response = requests.post(url, json=payload)
    
    if response.status_code == 200:
        result = response.json()
        print("\n=== COMPARISON RESULT ===")
        print(f"Similarity Score: {result.get('similarity_score', 'N/A')}")
        print(f"Match: {result.get('match', 'N/A')}")
        print(f"Confidence: {result.get('confidence', 'N/A')}")
        
        # Detailed analysis
        analysis = result.get('detailed_analysis', {})
        print(f"\nDetailed Analysis:")
        print(f"- Detection Method: {analysis.get('detection_method', 'N/A')}")
        print(f"- Processing Time: {analysis.get('processing_time_ms', 'N/A')} ms")
        print(f"- Face Detection Status: {analysis.get('face_detection_status', 'N/A')}")
        
        # Expected result: identical images should have high similarity (>0.9)
        similarity = result.get('similarity_score', 0)
        if similarity > 0.95:
            print("\n✅ SUCCESS: Identical images correctly identified (similarity > 0.95)")
        elif similarity > 0.8:
            print("\n⚠️  WARNING: Moderate similarity for identical images (0.8-0.95)")
        else:
            print(f"\n❌ FAIL: Low similarity for identical images ({similarity})")
            
    else:
        print(f"Error: {response.status_code}")
        print(response.text)

if __name__ == "__main__":
    test_identical_comparison()
