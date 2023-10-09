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
	Button
} from "@chakra-ui/react";
import { Container, Row, Col } from "react-bootstrap";
import { useGetAccountInfo } from "@multiversx/sdk-dapp/hooks/account";
import { logout } from "@multiversx/sdk-dapp/utils";
import {
	ExtensionLoginButton,
	WalletConnectLoginButton,
	LedgerLoginButton,
	WebWalletLoginButton,
} from "@multiversx/sdk-dapp/UI";

function XSignInModal({isOpen, onClose}) {
	return (
		<Modal isOpen={isOpen} onClose={onClose}>
			<ModalOverlay />
			<ModalContent textAlign={"center"}>
				<ModalHeader>Connect to a wallet</ModalHeader>
				<ModalCloseButton />
				<Row>
					<Col className="text-center">
						<WebWalletLoginButton
							callbackRoute="/"
							nativeAuth={true}
							loginButtonText="Web wallet"
							className="btn btn-sm dapp-primary font-size-sm w-60"
						/>
					</Col>
				</Row>
				<Row>
					<Col>
						<LedgerLoginButton
							loginButtonText="Ledger"
							nativeAuth={true}
							callbackRoute="/"
							className="btn btn-sm dapp-primary font-size-sm w-60"
						/>
					</Col>
				</Row>
				<Row>
					<Col>
						<WalletConnectLoginButton
							callbackRoute="/"
							nativeAuth={true}
							loginButtonText={"xPortal App"}
							isWalletConnectV2={true}
							className="btn btn-sm dapp-primary font-size-sm w-60"
						/>
					</Col>
				</Row>
				<Row className="mb-4">
					<Col>
						<ExtensionLoginButton
							callbackRoute="/"
							nativeAuth={true}
							loginButtonText="Extension"
							className="btn btn-block btn-sm dapp-primary font-size-sm w-60 mt-1"
						/>
					</Col>
				</Row>
			</ModalContent>
		</Modal>
	);
}

export default XSignInModal;