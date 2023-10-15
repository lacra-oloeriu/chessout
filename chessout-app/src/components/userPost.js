import React, {useState} from 'react';
import { Avatar, Typography, CardHeader, CardContent, CardMedia, Card, CardActions, IconButton } from "@mui/material";
import {Button} from "react-bootstrap";
import { Collapse } from '@mui/material';
import FavoriteIcon from '@mui/icons-material/Favorite';
import FavoriteBorderIcon from '@mui/icons-material/FavoriteBorder';
import SmsOutlinedIcon from '@mui/icons-material/SmsOutlined';
import Divider from "@mui/material/Divider";
import Tooltip from "@mui/material/Tooltip";
import Fade from "@mui/material/Fade";
import Menu from '@mui/material/Menu';
import MenuItem from '@mui/material/MenuItem';
import MoreVertIcon from '@mui/icons-material/MoreVert';
import ArrowUpwardIcon from '@mui/icons-material/ArrowUpward';
import ArrowDownwardIcon from '@mui/icons-material/ArrowDownward';

const componentsProps={
	tooltip: {
		sx: {
			maxWidth: '200px',
			backgroundColor: 'black',
			color: 'white',
			fontSize: '14px',
			fontWeight: '400',
			textAlign: 'center',
			borderRadius: '10px',
			padding: '10px',
			top: '-10px'
		},
	},
	arrow: {
		sx: {
			color: 'black',
		},
	},
	TransitionComponent: Fade,
}

const UserPost = ({image, title, user, time, avatar, likesCount, likes, commentsCount, comments, isLikedByCurrentUser}) => {
	const [isCommentVisible, setIsCommentVisible] = useState(false);

	const toggleCommentVisibility = () => {
		setIsCommentVisible(!isCommentVisible);
	};

	const [anchorEl, setAnchorEl] = useState(null);
	const handleMenuOpen = (event) => {
		setAnchorEl(event.currentTarget);
	};
	const handleMenuClose = () => {
		setAnchorEl(null);
	};

	return (
		<Card className="b-r-sm">
			<CardHeader
				avatar={
					<Avatar aria-label="User" src={avatar}>
						U
					</Avatar>
				}
				title={
					<React.Fragment>
						<span>{user}</span>
						<div className="float-end">
							<IconButton
								aria-controls="post-menu"
								aria-haspopup="true"
								onClick={handleMenuOpen}
							>
								<MoreVertIcon />
							</IconButton>
							<Menu
								id="post-menu"
								anchorEl={anchorEl}
								keepMounted
								open={Boolean(anchorEl)}
								onClose={handleMenuClose}
								style={{
									transform: 'translateX(-4.1%)',
								}}
							>
								<MenuItem onClick={handleMenuClose}>Delete post</MenuItem>
							</Menu>
						</div>
					</React.Fragment>
				}
				subheader={time}
			/>
			<CardContent style={{marginTop: '-15px'}}>
				<Typography variant="body1" color="textPrimary">
					{title}
				</Typography>
			</CardContent>
			<Divider color={"#2f2f2f"} />
			<div className="d-flex justify-content-center" style={{backgroundColor: '#2f2f2f'}}>
				<CardMedia
					component="img"
					alt="Post Image"
					image={image}
					title="Post Image"
					style={{
						maxHeight: '600px',
						width: 'auto'
					}}
				/>
			</div>
			<Divider color={"#2f2f2f"} />
			<CardActions className="d-flex justify-content-between">
				<div>
					{isLikedByCurrentUser ? (
						<Tooltip key="unlike" title="Unlike the post" arrow placement="bottom" componentsProps={componentsProps}>
							<IconButton aria-label="unlike" size="small" className="text-danger">
								<FavoriteIcon fontSize="small" />
							</IconButton>
						</Tooltip>
					):(
						<Tooltip key="like" title="Like the post" arrow placement="bottom" componentsProps={componentsProps}>
							<IconButton aria-label="Like" size="small" className="text-danger">
								<FavoriteBorderIcon />
							</IconButton>
						</Tooltip>
					)}
					<span className="font-size-xs">{likesCount} Likes</span>
				</div>
				<div>
					<Tooltip key="comment" title={!isCommentVisible ? `Show comments section` : 'Hide comments section'} arrow placement="bottom" componentsProps={componentsProps}>
						<Button variant="link" className="font-size-xs text-light text-decoration-none" onClick={toggleCommentVisibility}>{commentsCount} Comments</Button>
					</Tooltip>
				</div>
			</CardActions>

			<Collapse in={isCommentVisible}>
				<Divider color={"#2f2f2f"} />
				{commentsCount? (
					comments.map((comment) =>(
						<React.Fragment key={comment.chatId + comment.userId}>
							<Divider color={"#2f2f2f"} style={{width: '90%', marginLeft: '11%', marginTop: '-1px'}}/>
							<CardHeader
								avatar={
									<Avatar aria-label="User" src={comment.userImage?.img_src} size={"small"}>
										U
									</Avatar>
								}
								title={
									<div>
										<Typography variant="subtitle2" component="span">
											{comment?.userName}
										</Typography>
										<Typography variant="caption" component="span" className="ms-1">
											{comment.time ? (` - ${comment.time}`):('')}
										</Typography>
									</div>
								}
								subheader={
									<div>
										<Typography variant="body2" component="span" className="text-justified">
											{comment?.textValue}
										</Typography>
									</div>
								}

							/>
						</React.Fragment>
					))
				) : (
					''
				)}
			</Collapse>
		</Card>
	);
};

export default UserPost;
