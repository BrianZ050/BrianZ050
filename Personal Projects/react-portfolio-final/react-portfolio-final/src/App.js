import { Routes, Route, useLocation } from "react-router-dom";
import { AnimatePresence } from "framer-motion";
import { lazy, Suspense } from "react";
import GlobalStyle from "./globalStyles";
import { ThemeProvider } from "styled-components";
import { lightTheme } from "./components/Themes";
import Loading from "./subComponents/Loading";

//Components
const Main = lazy(() => import("./components/Main"));
const AboutPage = lazy(() => import("./components/AboutPage"));
const MySkillsPage = lazy(() => import("./components/MySkillsPage"));
const CertificatesPage = lazy(() => import("./components/CertificatesPage"));
const ExperiencePage = lazy(() => import("./components/ExperiencePage"));
const SoundBar = lazy(() => import("./subComponents/SoundBar"));
const ContactPage = lazy(() => import("./components/ContactPage"));

function App() {
  const location = useLocation();

  return (
    <>
      <GlobalStyle />

      <ThemeProvider theme={lightTheme}>
        <Suspense fallback={<Loading />}>
          <SoundBar />
          {/* Changed prop from exitBefore to mode */}
          <AnimatePresence mode="wait">
            {/* Changed Switch to Routes */}

            <Routes location={location} key={location.pathname}>
              {/* Changed component to element */}

              <Route path="/" element={<Main />} />

              <Route path="/about" element={<AboutPage />} />

              <Route path="/certificates" element={<CertificatesPage />} />

              <Route path="/experience" element={<ExperiencePage />} />

              <Route path="/skills" element={<MySkillsPage />} />
              
              <Route path="/contact" element={<ContactPage />} />

              <Route path="*" element={<Main />} />
            </Routes>
            
          </AnimatePresence>
        </Suspense>
      </ThemeProvider>
    </>
  );
}

export default App;
