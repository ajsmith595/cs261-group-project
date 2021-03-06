import React from "react";
import { Form, Col, Row, Button, Card } from "react-bootstrap";
import { Redirect } from "react-router-dom";
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faUser, faExclamationCircle } from '@fortawesome/free-solid-svg-icons';
export default class RegisterView extends React.Component {
    constructor(props) {
        super(props);
        this.sendStateToServer = this.sendStateToServer.bind(this);

        this.state = {
            acceptTerms: false,
            email: "",
            username: "",
            serverError: "",
            error: [],
            status: 'main',
            emailErrorActive: false,
            usernameErrorActive: false,
        };
    }
    componentDidMount() {
        document.title = "Register";
    }

    /*
    Sends the state to the server. It first verifies the fields
    for them being empty and having a valid email, and
    if they have accepted the terms
    */
    sendStateToServer(e) {
        e.preventDefault();
        var errors = [];
        if (this.state.acceptTerms == false) {
            errors.push("Please accept the Terms");
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
        if (!(new RegExp(/[a-z0-9]{4,16}/g).test(this.state.username))) {
            errors.push("Please enter a username that is alphanumeric and between 4 to 16 characters");
        }
        if (errors != "") {
            this.setState({// Stores the set of errors to be shown to the user
                error: errors
            });
        } else {
            this.setState({
                status: 'loading'
            });
            fetch((process.env.REACT_APP_HTTP_ADDRESS || "") + '/api/register',
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
                    throw new Error("Bad response from server");
                }
                response.json().then(data => {
                    if (data.status == "error") {//Custom error sent from server
                        this.setState({//Empties the errors from validation
                            error: [],
                            status: 'main',
                            serverError: data.message
                        });
                    } else if (data.status == "success") {
                        this.setState({
                            status: 'success'
                        });
                        this.props.history.push("/");
                    }
                })
            }).catch(e => {
                this.setState({
                    serverError: 'Something went wrong. Please check your internet connection',
                    status: 'main'
                });
            });
        }

    }

    /* 
    Renders the Registration View
    */
    render() {
        let emailError = !(new RegExp(/[a-z0-9._%+-]+@[a-z0-9.-]+\.[a-z]{2,15}/g).test(this.state.email));
        let usernameError = !(new RegExp(/[a-z0-9]{4,16}/g).test(this.state.username));
        console.log("test");
        let disabled = this.state.status === 'success' || this.state.status == "loading" || emailError || usernameError || !this.state.acceptTerms;
        return (
            <div className="py-2">
                <h1 className="text-center">Register</h1>
                <hr />
                <Row>
                    <Col xs={0} sm={1} md={3}></Col>
                    <Col xs={12} sm={10} md={6}>
                        <div id="Error message"><p>{this.renderErrors()}</p></div>
                        <p>
                            Please register your email. This will only be used for identification purposes. If you choose
                            to be anonymous when providing feedback, your username will not be visible to the host or any
                            attendees. Your email will not be visible to any other users.
                        </p>
                        <Form id="feedback" onSubmit={(e) => { if (!disabled) { this.sendStateToServer(e) } else { this.setState({ emailErrorActive: true, usernameErrorActive: true }) } }}>
                            <Form.Group>
                                <Form.Label>Email</Form.Label>
                                <Form.Control className={"mx-auto " + ((emailError && this.state.emailErrorActive) ? 'border-danger' : '')} onChange={(e) => this.setState({ email: e.target.value })} value={this.state.email} onBlur={() => this.setState({ emailErrorActive: true })} placeholder="Enter email" as="input"></Form.Control>
                            </Form.Group>
                            <Form.Group>
                                <Form.Label>Username</Form.Label>
                                <Form.Control className={"mx-auto " + ((usernameError && this.state.usernameErrorActive) ? 'border-danger' : '')} onChange={(e) => this.setState({ username: e.target.value })} value={this.state.username} placeholder="Enter username" as="input" onBlur={() => this.setState({ usernameErrorActive: true })}></Form.Control>
                            </Form.Group>
                            <Form.Check type="checkbox" id="agree_check" >
                                <Form.Check.Input type="checkbox" checked={this.state.acceptTerms} onChange={(e) => this.setState({ acceptTerms: e.target.checked })} />
                                <Form.Check.Label>I agree to the terms.</Form.Check.Label>
                            </Form.Check>
                            <hr />
                            <Button className="w-100" type="submit" variant="primary" onClick={(e) => this.sendStateToServer(e)}><FontAwesomeIcon icon={faUser} /> Register</Button>
                        </Form>
                    </Col>

                    <Col xs={0} sm={1} md={3}></Col>
                </Row>
            </div >
        );
    }

    /*
    Returns the Form input for the email field
    */
    renderEmail() {
        return (
            <Form.Group controlId={"email"}>
                <Form.Label>{"email:"}</Form.Label>
                <Row>
                    <Col xs={0} sm={1} md={3}></Col>
                    <Col xs={12} sm={10} md={6}>
                        <Form.Control className="mx-auto" onChange={(e) => this.setState({ email: e.target.value })} value={this.state.email} placeholder="Enter email" as="input"></Form.Control>
                    </Col>
                    <Col xs={0} sm={1} md={3}></Col>
                </Row>
            </Form.Group >
        );
    }

    /*
    Returns the Form input for the username field
    */
    renderUsername() {
        return (
            <Form.Group controlId={"username"}>
                <Form.Label>{"username:"}</Form.Label>
                <Row>
                    <Col xs={0} sm={1} md={3}></Col>
                    <Col xs={12} sm={10} md={6}>
                        <Form.Control className="mx-auto" onChange={(e) => this.setState({ username: e.target.value })} value={this.state.username} placeholder="Enter username" as="input"></Form.Control>
                    </Col>
                    <Col xs={0} sm={1} md={3}></Col>
                </Row>
            </Form.Group >
        );
    }

    /* 
    Renders the list of errors, both validation errors and from the server
    */
    renderErrors() {
        let errors = [];
        for (let text in this.state.error) {
            errors.push(<li>{this.state.error[text]}</li>);
        }
        if (this.state.serverError != "") {
            errors.push(<li>{this.state.serverError}</li>);
        }
        if (errors.length == 0) return null;
        return (
            <Card className="" style={{ borderColor: '#f5c6cb' }}>
                <Card.Header style={{ fontVariant: 'small-caps', fontSize: "100%" }} className="alert alert-danger m-0 py-1 px-3"><FontAwesomeIcon icon={faExclamationCircle} /> errors</Card.Header>
                <Card.Body className="py-2 px-4">
                    <ul className="list-unstyled m-0">
                        {errors}
                    </ul>
                </Card.Body>
            </Card>
        )
    }
}