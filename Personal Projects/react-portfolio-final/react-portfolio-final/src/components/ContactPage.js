import React, { useRef } from 'react';
import styled, { ThemeProvider } from 'styled-components';
import emailjs from '@emailjs/browser';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faEnvelope } from '@fortawesome/free-solid-svg-icons';
import PowerButton from '../subComponents/PowerButton';
import SocialIcons from '../subComponents/SocialIcons';
import LogoComponent from '../subComponents/LogoComponent';
import Loading from '../subComponents/Loading';
import { lazy, Suspense } from 'react';
import { motion } from 'framer-motion';
import { DarkTheme } from './Themes';
import { mediaQueries } from './Themes';

const ParticleComponent = lazy(() =>
  import("../subComponents/ParticleComponent")
);

const Container = styled(motion.div)`
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 2rem;
  position: relative; /* Ensure positioning context for z-index */

  ${mediaQueries(40)`
    padding: 1rem;
  `};
  ${mediaQueries(25)`
    padding: 0.5rem;
  `};
`;

const Form = styled.form`
  display: flex;
  flex-direction: column;
  width: 100%;
  max-width: 500px;
  z-index: 1; /* Ensure form is above particles */

  ${mediaQueries(40)`
    max-width: 400px;
  `};
  ${mediaQueries(25)`
    max-width: 300px;
  `};
`;

const Input = styled.input`
  margin-bottom: 1rem;
  padding: 0.5rem;
  font-size: 1rem;

  ${mediaQueries(40)`
    padding: 0.4rem;
    font-size: 0.9rem;
  `};
  ${mediaQueries(25)`
    padding: 0.3rem;
    font-size: 0.8rem;
  `};
`;

const TextArea = styled.textarea`
  margin-bottom: 1rem;
  padding: 0.5rem;
  font-size: 1rem;

  ${mediaQueries(40)`
    padding: 0.4rem;
    font-size: 0.9rem;
  `};
  ${mediaQueries(25)`
    padding: 0.3rem;
    font-size: 0.8rem;
  `};
`;

const Button = styled.button`
  padding: 0.5rem;
  font-size: 1rem;
  background-color: #007BFF;
  color: white;
  border: none;
  cursor: pointer;

  &:hover {
    background-color: #0056b3;
  }

  ${mediaQueries(40)`
    padding: 0.4rem;
    font-size: 0.9rem;
  `};
  ${mediaQueries(25)`
    padding: 0.3rem;
    font-size: 0.8rem;
  `};
`;

const EmailSign = styled.div`
  display: flex;
  align-items: center;
  margin-bottom: 1rem;
  padding: 0.5rem;
  font-size: 1rem;

  ${mediaQueries(40)`
    padding: 0.4rem;
    font-size: 0.9rem;
  `};
  ${mediaQueries(25)`
    padding: 0.3rem;
    font-size: 0.8rem;
  `};
`;

const EmailLink = styled.a`
  color: inherit;
  text-decoration: none;
  display: flex;
  align-items: center;

  &:hover {
    text-decoration: underline;
  }
    z-index: 1; /* Ensure form is above particles */

  ${mediaQueries(40)`
    font-size: 0.9rem;
  `};
  ${mediaQueries(25)`
    font-size: 0.8rem;
  `};
`;

const ContactPage = () => {
  const form = useRef();

  const sendEmail = (e) => {
    e.preventDefault();

    emailjs
      .sendForm('service_7d2louf', 'template_ksejhjq', form.current, 'vdCTLdjs62sgEAXKP')
      .then(
        () => {
          console.log('SUCCESS!');
        },
        (error) => {
          console.log('FAILED...', error.text);
        },
      );
  };

  return (
    <ThemeProvider theme={DarkTheme}>
      <Container
        key="contact"
        initial={{ opacity: 0 }}
        animate={{ opacity: 1, transition: { duration: 1 } }}
        exit={{ opacity: 0, transition: { duration: 0.5 } }}
      >
        <ParticleComponent theme="light" />
        <LogoComponent theme="dark" />
        <PowerButton />
        <SocialIcons theme="white" />
        <Suspense fallback={<Loading />}>
          <h1 style={{ marginTop: '4rem' }}>Contact Me</h1>
          <EmailSign>
            <EmailLink
              href="https://mail.google.com/mail/?view=cm&fs=1&to=Brian.Zhang05@gmail.com"
              target="_blank"
              rel="noopener noreferrer"
            >
              <FontAwesomeIcon icon={faEnvelope} style={{ marginRight: '0.5rem' }} />
              Brian.Zhang05@gmail.com
            </EmailLink>
          </EmailSign>
          <Form ref={form} onSubmit={sendEmail}>
            <label>Name</label>
            <Input type="text" name="name" placeholder="Your Name" required />
            <label>Email</label>
            <Input type="email" name="email" placeholder="Your Email" required />
            <label>Subject</label>
            <Input type="text" name="subject" placeholder="Subject" required />
            <label>Message</label>
            <TextArea name="message" rows="5" placeholder="Your Message" required />
            <Button type="submit">Send</Button>
          </Form>
        </Suspense>
      </Container>
    </ThemeProvider>
  );
};

export default ContactPage;