import React from 'react';
import { ChakraProvider, CSSReset, extendTheme } from '@chakra-ui/react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import NavBar from './components/NavBar';
import About from './pages/about';
import ClubPlayers from './pages/club_players';
import Dashboard from './pages/dashboard';
import Home from './pages/home';
import MyClubs from './pages/my_clubs';
import MyProfile from './pages/my_profile';
import Team from './pages/team';
import Tournaments from './pages/tournaments';

import { DappProvider } from "@multiversx/sdk-dapp/wrappers/DappProvider";
import { NotificationModal, SignTransactionsModals, TransactionsToastList } from "@multiversx/sdk-dapp/UI";
import { networkId} from "./config/customConfig";
import { networkConfig } from "./config/networks";

const theme = extendTheme({
  // Chakra UI theme customization
});

function App() {

  //Set the config network
  const customNetConfig = networkConfig[networkId];

  return (
    <DappProvider
      environment={customNetConfig.id}
      customNetworkConfig={customNetConfig}
      dappConfig={{ shouldUseWebViewProvider: true }}
      completedTransactionsDelay={200}
    >
      <ChakraProvider theme={theme}>
        <CSSReset />
        <Router>
          <NavBar />
          <Routes>
            <Route path="/" element={<Home />} />
            <Route path="/about" element={<About />} />
            <Route path="/dashboard" element={<Dashboard />} />
            <Route path="/my_clubs" element={<MyClubs />} />
            <Route path="/my_profile" element={<MyProfile />} />
            <Route path="/club_players" element={<ClubPlayers />} />
            <Route path="/team" element={<Team />} />
            <Route path="/tournaments" element={<Tournaments />} />
          </Routes>
        </Router>
      </ChakraProvider>
    </DappProvider>
  );
}

export default App;