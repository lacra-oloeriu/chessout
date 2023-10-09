// SignInModal.js
import React, { useState } from 'react';
import {
	Modal,
	ModalOverlay,
	ModalContent,
	ModalHeader,
	ModalFooter,
	ModalBody,
	ModalCloseButton,
	Tab,
	TabList,
	TabPanel,
	TabPanels,
	Tabs,
	Input,
	Stack,
	Flex,
	Grid,
	GridItem,
	Button
} from "@chakra-ui/react";
import {
	Container,
	Row,
	Col,
	FloatingLabel,
	Form
} from "react-bootstrap";
import {
	createUserWithEmailAndPassword,
	GoogleAuthProvider,
	signInWithEmailAndPassword,
	signInWithPopup
} from "firebase/auth";

const firebaseErrorMessages = {
	"auth/invalid-email": "Invalid Email",
	"auth/invalid-login-credentials": "Invalid Credentials",
	"auth/weak-password": "Password should be at least 6 characters",
	// Add more mappings for other Firebase error codes as needed
};

function SignInModal({ isOpen, onClose, authInstance }) {
	const [activeTab, setActiveTab] = useState('sign-in'); // Default to sign-in tab
	const [email, setEmail] = useState('');
	const [password, setPassword] = useState('');
	const [errorMessage, setErrorMessage] = useState(null);

	const handleTabChange = (tab) => {
		setActiveTab(tab);
	};

	const handleSignInWithGoogle = async () => {
		const provider = new GoogleAuthProvider();
		try {
			await signInWithPopup(authInstance, provider);
		} catch (error) {
			console.error(error);
		}
	};

	const handleSignInWithEmailAndPassword = async (email, password) => {
		try {
			await signInWithEmailAndPassword(authInstance, email, password);
			onClose();
		} catch (error) {
			const errorFirebaseMessage = firebaseErrorMessages[error.code] || error.message; // Use custom message if available, or the original message
			setErrorMessage(errorFirebaseMessage);
		}
	};

	const handleSignUpWithEmailAndPassword = async (email, password) => {
		try {
			await createUserWithEmailAndPassword(authInstance, email, password);
			onClose();
		} catch (error) {
			const errorFirebaseMessage = firebaseErrorMessages[error.code] || error.message; // Use custom message if available, or the original message
			setErrorMessage(errorFirebaseMessage);
		}
	};

	return (
		<Modal isOpen={isOpen} onClose={onClose}>
			<ModalOverlay />
			<ModalContent>

				<ModalCloseButton />
				<Tabs isFitted variant="enclosed">
					<TabList>
						<Tab onClick={() => handleTabChange('sign-in')}>Sign In</Tab>
						<Tab onClick={() => handleTabChange('sign-up')}>Sign Up</Tab>
					</TabList>
					<TabPanels>
						<TabPanel>
							<Container>
								<Row>
									<Col xs={12}>
										<FloatingLabel
											controlId="floatingInputSignIn"
											label="Email address"
											className="mb-3"
										>
											<Form.Control
												type="email"
												placeholder="name@example.com"
												value={email}
												onChange={(e) => setEmail(e.target.value)}
											/>
										</FloatingLabel>
										<FloatingLabel
											controlId="floatingPasswordSignIn"
											label="Password"
										>
											<Form.Control
												type="password"
												placeholder="Password"
												value={password}
												onChange={(e) => setPassword(e.target.value)}
											/>
										</FloatingLabel>
										{errorMessage && <p className="text-danger text-center mt-2">{errorMessage}</p>}
									</Col>
								</Row>
								<Row className="mt-3">
									<Col xs={6}>
										<Button colorScheme="blue" onClick={() => handleSignInWithEmailAndPassword(email, password)}>
											Sign In with Email
										</Button>
									</Col >
									<Col xs={6}>
										<Button colorScheme="red" onClick={() => handleSignInWithGoogle()}>
											Sign In with Google
										</Button>
									</Col>
								</Row>
							</Container>
						</TabPanel>
						<TabPanel>
							<Container>
								<Row>
									<Col xs={12}>
										<FloatingLabel
											controlId="floatingInputSignUp"
											label="Email address"
											className="mb-3"
										>
											<Form.Control
												type="email"
												placeholder="name@example.com"
												value={email}
												onChange={(e) => setEmail(e.target.value)}
											/>
										</FloatingLabel>
										<FloatingLabel
											controlId="floatingPasswordSignUp"
											label="Password"
										>
											<Form.Control
												type="password"
												placeholder="Password"
												value={password}
												onChange={(e) => setPassword(e.target.value)}
											/>
										</FloatingLabel>
										{errorMessage && <p className="text-danger text-center mt-2">{errorMessage}</p>}
									</Col>
								</Row>
								<Row className="mt-3">
									<Col xs={6}>
										<Button colorScheme="green" onClick={() => handleSignUpWithEmailAndPassword(email, password)}>
											Sign Up with Email
										</Button>
									</Col >
									<Col xs={6}>
										<Button colorScheme="red" onClick={() => handleSignInWithGoogle()}>
											Sign Up with Google
										</Button>
									</Col>
								</Row>
							</Container>
						</TabPanel>
					</TabPanels>
				</Tabs>
			</ModalContent>
		</Modal>
	);
}

export default SignInModal;