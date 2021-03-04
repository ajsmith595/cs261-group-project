import React from "react";
import { Form, Col, Row, Button, ListGroup } from "react-bootstrap";
import { Redirect } from "react-router-dom";
export default class RegisterView extends React.Component {
    constructor(props) {
        super(props);
        this.sendStateToServer = this.sendStateToServer.bind(this);

        this.state = {
            email: "",
            username: "",
            serverError: "",
            error: [],
            status: 'main'
        };
    }

    componentDidMount() {
        document.title = "Login";
    }

    /*
    Sends the state to the server. It first verifies the fields
    for them being empty and having a valid email
    */
    sendStateToServer() {
        var errors = [];
        if (this.state.acceptTerms == false) {
            errors.push("Please accept the Terms");
        }
        if (this.state.username == "") {
            errors.push("Please enter a username");
        }
        //Tests the email against a regular expression
        /* The expression requires:
            at least 1 character before the @
            An @ symbol
            At least 1 character between the @ and .
            Between 2-15 characters after the .
        */
        if (!(new RegExp(/[a-z0-9._%+-]+@[a-z0-9.-]+\.[a-z]{2,15}/g).test(this.state.email))) {
            errors.push("Please enter a valid email");
        }
        if (errors != "") {
            this.setState({// Stores the set of errors to be shown to the user
                error: errors,
            });
        } else {
            this.setState({
                status: 'loading'
            })
            fetch((process.env.REACT_APP_HTTP_ADDRESS || "") + '/api/login',
                {
                    method: 'POST',
                    headers: {
                        'Accept': 'application/json',
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(this.state),// Sends the state to the server
                    credentials: "include"
                }
            ).then(response => {
                if (response.status >= 400 || !response.ok) {//Throws an error if the server responds witha error status
                    this.setState({
                        status: 'error'
                    });
                }
                response.json().then(data => {
                    if (data.status == "error") {//Custom error sent from server
                        this.setState({
                            error: [], //Empties the errors from validation
                            serverError: data.message,
                            status: 'main'
                        });
                    } else if (data.status == "success") {
                        this.setState({
                            status: 'success'
                        });
                        this.props.history.push("/");
                    }
                })
            }).catch(e => {
                console.log(e);
                this.setState({
                    error: [],
                    serverError: e.getMessage(),
                    status: 'main',
                })
            });
        }
    }

    /* 
    Renders the Login View
    */
    render() {
        if (this.state.status == 'error') {
            return <div className="text-center">
                <h1>An error occured</h1>
                <Button onClick={() => this.setState({ status: 'main' })}>Retry</Button>
            </div>
        }
        return (
            <div className="py-2">
                <h1 className="text-center">Login</h1>
                <hr />
                <Row>
                    <Col xs={0} sm={1} md={3}></Col>
                    <Col xs={12} sm={10} md={6}>
                        <div id="Error message"><p>{this.renderErrors()}</p></div>
                        <Form id="feedback">
                            <Form.Group>
                                <Form.Label for="login_email_input">Email</Form.Label>
                                <Form.Control id="login_email_input" className="mx-auto" onChange={(e) => this.setState({ email: e.target.value })} value={this.state.email} type="email" placeholder="Enter email" as="input"></Form.Control>
                            </Form.Group>
                            <Form.Group>
                                <Form.Label for="login_username_input">Username</Form.Label>
                                <Form.Control id="login_username_input" className="mx-auto" onChange={(e) => this.setState({ username: e.target.value })} value={this.state.username} placeholder="Enter username" as="input"></Form.Control>
                            </Form.Group>
                            <hr />
                            <Button className="w-100" type="button" variant="primary" onClick={this.sendStateToServer} disabled={this.state.status === 'success' || this.state.status === 'loading'}>Submit</Button>
                        </Form>
                    </Col>
                    <Col xs={0} sm={1} md={3}></Col>
                </Row>
            </div >
        );
    }


    /* 
    Renders the list of errors, both validation errors and from the server
    */
    renderErrors() {
        let errors = []
        for (let text in this.state.error) {
            errors.push(<ListGroup.Item>{this.state.error[text]}</ListGroup.Item>);
        }
        if (this.state.serverError != "") {
            errors.push(<ListGroup.Item>{this.state.serverError}</ListGroup.Item>);
        }
        return (
            <ListGroup variant="flush">
                {errors}
            </ListGroup>
        )
    }
}