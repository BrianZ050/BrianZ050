import React, { useEffect, useRef } from 'react';
import * as THREE from 'three';
import { GLTFLoader } from 'three/addons/loaders/GLTFLoader.js';
import { OrbitControls } from 'three/addons/controls/OrbitControls.js';

const ModelComponent = () => {
  const mountRef = useRef(null);

  useEffect(() => {
    const scene = new THREE.Scene();
    const camera = new THREE.PerspectiveCamera(75, window.innerWidth / window.innerHeight, 0.1, 1000);

    let mouseX = window.innerWidth / 2;
    let mouseY = window.innerHeight / 2;
    let object;
    let controls;
    const objToRender = 'eye';

    const loader = new GLTFLoader();
    loader.load(
      `models/${objToRender}/scene.gltf`,
      function (gltf) {
        object = gltf.scene;
        scene.add(object);
      },
      function (xhr) {
        console.log((xhr.loaded / xhr.total * 100) + '% loaded');
      },
      function (error) {
        console.error(error);
      }
    );

    const renderer = new THREE.WebGLRenderer({ alpha: true });
    renderer.setSize(window.innerWidth, window.innerHeight);
    mountRef.current.appendChild(renderer.domElement);

    camera.position.z = objToRender === "dino" ? 25 : 500;

    const topLight = new THREE.DirectionalLight(0xffffff, 1);
    topLight.position.set(500, 500, 500);
    topLight.castShadow = true;
    scene.add(topLight);

    const ambientLight = new THREE.AmbientLight(0x333333, objToRender === "dino" ? 5 : 1);
    scene.add(ambientLight);

    if (objToRender === "dino") {
      controls = new OrbitControls(camera, renderer.domElement);
    }

    const animate = () => {
      requestAnimationFrame(animate);

      if (object && objToRender === "eye") {
        object.rotation.y = -3 + mouseX / window.innerWidth * 3;
        object.rotation.x = -1.2 + mouseY * 2.5 / window.innerHeight;
      }

      renderer.render(scene, camera);
    };

    const handleResize = () => {
      camera.aspect = window.innerWidth / window.innerHeight;
      camera.updateProjectionMatrix();
      renderer.setSize(window.innerWidth, window.innerHeight);
    };

    const handleMouseMove = (e) => {
      mouseX = e.clientX;
      mouseY = e.clientY;
    };

    window.addEventListener('resize', handleResize);
    document.addEventListener('mousemove', handleMouseMove);

    animate();

    return () => {
      window.removeEventListener('resize', handleResize);
      document.removeEventListener('mousemove', handleMouseMove);
      mountRef.current.removeChild(renderer.domElement);
    };
  }, []);

  return <div id="container3D" ref={mountRef} />;
};

export default ModelComponent;