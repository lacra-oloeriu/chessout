import React from "react";
//import { Fragment } from "react";

import NavBar from "./components/NavBar";
import { ChakraProvider } from "@chakra-ui/react";

function App() {
  return (
    <ChakraProvider>
      <NavBar />
    </ChakraProvider>
  );
}

export default App;
