import React from "react";
import { Redirect } from "react-router-dom";
import { Button } from 'react-bootstrap';
export default class LogoutView extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            status: 'loading'
        };
    }

    componentDidMount() {
        document.title = "Logging out...";
        this.setState({
            status: 'loading'
        });
        fetch((process.env.REACT_APP_HTTP_ADDRESS || "") + `/api/logout`, {
            method: "POST",
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            },
            credentials: "include"
        }).then(e => e.json()).then(e => {
        }).catch(e => {
        });
        this.props.history.push("/login");
    }
    render() {
        if (this.state.status == 'loading') {
            return (
                <h1 class="text-center">Loading...</h1>
            );
        } else if (this.state.status == 'error') {
            return (
                <div class="text-center">
                    <h1 >An error occured</h1>
                    <Button onClick={() => this.componentDidMount()}>Retry</Button>
                </div>
            );
        } else {
            return null;
        }

    }
}