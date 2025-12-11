import { motion } from 'framer-motion';
import React, { useState, useEffect } from 'react'
import { NavLink } from 'react-router-dom';
import styled from 'styled-components';
import { Github } from '../components/AllSvgs';



const Box = styled(motion.li)`
width: ${props => props.embed ? '30rem' : (props.media ? '20rem' : '16rem')};
height: ${props => props.media ? '48vh' : '40vh'};
background-color: ${props => props.theme.text};
color:${props => props.theme.body};
padding: 1.5rem 2rem;
margin-right: 8rem;
border-radius: 0 50px 0 50px;
display: flex;
flex-direction: column;
justify-content: space-between;
border: 1px solid ${props => props.theme.body};
transition: all 0.2s ease;

&:hover{
background-color: ${props => props.theme.body};
color:${props => props.theme.text};
border: 1px solid ${props => props.theme.text};

}
`

const MediaWrapper = styled.div`
    width: 100%;
    display: flex;
    justify-content: center;
    align-items: center;
    margin: 0.5rem 0 0.75rem 0;
`

const MediaInner = styled.div`
    width: 100%;
    /* shrink-wrap when showing an image, use a larger preview height for embeds, otherwise use fixed height for videos */
    height: ${props => props.hasImage ? 'auto' : (props.hasEmbed ? '18rem' : '10rem')};
    display: flex;
    justify-content: center;
    align-items: center;
    overflow: ${props => (props.hasImage || props.hasEmbed) ? 'hidden' : 'hidden'};
    border-radius: 8px;
    background: ${props => props.hasImage ? 'transparent' : props.theme.body};
`

const Video = styled.video`
    width: 100%;
    height: 100%;
    object-fit: contain;
    background: #000;
`

const Img = styled.img`
    display: block;
    width: auto;
    height: auto;
    max-width: 100%;
    object-fit: contain;
    cursor: pointer;
`

const ModalOverlay = styled.div`
    position: fixed;
    top: 0;
    left: 0;
    width: 100vw;
    height: 100vh;
    background: rgba(0,0,0,0.85);
    display: flex;
    justify-content: center;
    align-items: center;
    padding: 1rem; /* keep content from touching edges on small screens */
    box-sizing: border-box;
    z-index: 1000;
`

const ModalContent = styled.div`
    position: relative;
    max-width: 90vw;
    max-height: 90vh;
    display: flex;
    justify-content: center;
    align-items: center;
    padding: 0.5rem;
    box-sizing: border-box;
        /* nudge the modal slightly up so large images don't appear too low */
        transform: translateY(-6vh);

        /* on very short viewports, avoid moving it off-screen */
        @media (max-height: 600px) {
            transform: none;
            align-items: flex-start;
        }
`

const ModalImage = styled.img`
        display: block;
        margin: 0 auto;
        /* make modal images smaller so they don't dominate the viewport */
        max-width: 60vw;
        max-height: 55vh;
        object-fit: contain;
        border-radius: 6px;

        /* on small screens allow the image to use more of the width while keeping height constrained */
        @media (max-width: 900px) {
            max-width: 84vw;
            max-height: 64vh;
        }

        @media (max-width: 480px) {
            max-width: 92vw;
            max-height: 68vh;
        }
`

const PreviewWrapper = styled.div`
    position: relative;
    width: 100%;
    height: 100%;
    overflow: hidden;
    padding: 0.5rem; /* increased inner padding to give the preview more breathing room */
    box-sizing: border-box;
`

const PreviewIframe = styled.iframe`
    width: 100%;
    height: 100%;
    border: 0;
    display: block;
    /* make preview interactive */
    pointer-events: auto;
    /* do not scale — show content at natural size for readability */
    transform: none;
    transform-origin: center top;
`

const PreviewOpenButton = styled.button`
    position: absolute;
    top: 6px;
    right: 6px;
    background: rgba(0,0,0,0.6);
    color: #fff;
    border: none;
    width: 34px;
    height: 34px;
    border-radius: 6px;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    z-index: 10;
    font-weight: 600;
    font-size: 0.9rem;
`

const ModalIframe = styled.iframe`
    width: 80vw;
    height: 80vh;
    max-width: 95vw;
    max-height: 95vh;
    border: 0;
    border-radius: 6px;
`

const CloseButton = styled.button`
    position: absolute;
    top: 8px;
    right: 8px;
    background: rgba(0,0,0,0.6);
    color: #fff;
    border: none;
    width: 36px;
    height: 36px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    z-index: 1001;
`
const Title = styled.h2`
font-size: calc(1em + 0.5vw);
`

const Description = styled.h2`
font-size: calc(0.8em + 0.3vw);
font-family: 'Karla',sans-serif;
font-weight: 500;
`
const Tags = styled.div`
border-top: 2px solid ${props =>props.theme.body};
padding-top: 0.5rem;
display:flex;
flex-wrap:wrap;
${Box}:hover &{
border-top: 2px solid ${props =>props.theme.text};
}
`
const Tag = styled.span`
margin-right:1rem;
font-size:calc(0.8em + 0.3vw);
`

const Footer = styled.footer`
display: flex;
justify-content: space-between;
`

const Link = styled.a`
background-color: ${props =>props.theme.body};
color: ${props =>props.theme.text};
text-decoration: none;
padding:0.5rem calc(2rem + 2vw);
border-radius: 0 0 0 50px;
font-size:calc(1em + 0.5vw);

${Box}:hover &{
    background-color: ${props =>props.theme.text};
    color: ${props =>props.theme.body};

}
`

const Git = styled.a`
color: inherit;
text-decoration: none;
${Box}:hover &{
    &>*{
        fill:${props =>props.theme.text};
    }
}

`

// Framer motion configuration
const Item = {
    hidden:{
        scale:0
    },
    show:{
        scale:1,
        transition: {
            type: 'spring',
            duration: 0.5
        }
    }
}

const Card = (props) => {

    const {id, name, description, tags, demo, github, video, image, embed} = props.data;
    const [isOpen, setIsOpen] = useState(false);
    const [modalSrc, setModalSrc] = useState(null);
    const [modalType, setModalType] = useState(null); // 'image' | 'embed'

    useEffect(() => {
        const onKey = (e) => {
            if(e.key === 'Escape') setIsOpen(false);
        }
        window.addEventListener('keydown', onKey);
        return () => window.removeEventListener('keydown', onKey);
    }, []);

    const openImage = (src) => {
        setModalSrc(src);
        setModalType('image');
        setIsOpen(true);
    }

    const openEmbed = (src) => {
        setModalSrc(src);
        setModalType('embed');
        setIsOpen(true);
    }

    const closeModal = () => {
        setIsOpen(false);
        setModalSrc(null);
        setModalType(null);
    }

        // helper to detect if video should be rendered as iframe (youtube/vimeo) or html5 video
        const isEmbed = (url) => {
            if(!url) return false;
            return /youtube\.com|youtu\.be|vimeo\.com/.test(url);
        }

        return (
                <Box key={id} variants={Item} media={!!(embed || video || image)} embed={!!embed}>
                        <Title>{name}</Title>

                        {/* Media area: embed, video or image (image opens modal) */}
                        {(embed || video || image) && (
                            <MediaWrapper>
                                <MediaInner hasImage={!video && !!image} hasEmbed={!!embed}>
                                        {embed && (
                                            <PreviewWrapper>
                                                <PreviewIframe
                                                    title={`${name}-site`}
                                                    src={embed}
                                                    sandbox="allow-scripts allow-same-origin allow-forms allow-popups"
                                                />
                                                <PreviewOpenButton aria-label={`Open ${name} full`} onClick={() => openEmbed(embed)}>⤢</PreviewOpenButton>
                                            </PreviewWrapper>
                                        )}
                                    {video && (
                                        isEmbed(video) ? (
                                            <iframe
                                                title={`${name}-embed`}
                                                src={video}
                                                width="100%"
                                                height="100%"
                                                frameBorder="0"
                                                allow="accelerometer; autoplay; encrypted-media; gyroscope; picture-in-picture"
                                                allowFullScreen
                                            />
                                        ) : (
                                            <Video controls>
                                                <source src={video} />
                                                Your browser does not support the video tag.
                                            </Video>
                                        )
                                    )}

                                    {!video && image && (
                                        <Img src={image} alt={name} onClick={() => openImage(image)} />
                                    )}
                                </MediaInner>
                            </MediaWrapper>
                        )}

                        <Description>
                                {description}
                        </Description>
                        <Tags>
                        {
                                        tags && tags.map((t,id) => {
                                                return <Tag key={id}>#{t}</Tag>
                                        })
                                }
                        </Tags>
                        <Footer>
                                <Link href={demo} target="_blank">
                                        Visit
                                </Link>
                                <Git  href={github}  target="_blank">
                                        <Github width={30} height={30} />
                                </Git>
                        </Footer>

                        {isOpen && modalSrc && (
                            <ModalOverlay onClick={closeModal}>
                                <ModalContent onClick={(e) => e.stopPropagation()}>
                                    {modalType === 'image' && (
                                        <ModalImage src={modalSrc} alt={name} />
                                    )}
                                    {modalType === 'embed' && (
                                        <ModalIframe
                                            title={`${name}-embed-full`}
                                            src={modalSrc}
                                            allowFullScreen
                                            sandbox="allow-scripts allow-same-origin allow-forms allow-popups"
                                        />
                                    )}
                                    <CloseButton onClick={closeModal} aria-label="Close">×</CloseButton>
                                </ModalContent>
                            </ModalOverlay>
                        )}

                </Box>
        )
}

export default Card
