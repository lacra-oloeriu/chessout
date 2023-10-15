import React from "react";
import { useParams } from 'react-router-dom';

function Tournament(props) {
	const { tournamentId } = useParams();

	return(
		<p>Tournament</p>
	);
}

export default Tournament;