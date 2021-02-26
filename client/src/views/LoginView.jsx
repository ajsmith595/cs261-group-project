import React from "react";
import { Form, Col, Row, Button, ListGroup } from "react-bootstrap";
export default class RegisterView extends React.Component {
    constructor(props) {
        super(props);
        this.sendStateToServer = this.sendStateToServer.bind(this);

        this.state = {
            email: "",
            username: "",
            serverError: "",
            error: []
        };
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
                error: errors
            });
        } else {
            fetch('/api/login',
                {
                    method: 'POST',
                    headers: {
                        'Accept': 'application/json',
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(this.state),// Sends the state to the server
                }
            ).then(response => {
                if (response.status >= 400) {//Throws an error if the server responds witha error status
                    throw new Error("Bad response from server");
                }
                response.json().then(data => {
                    if (data.status == "error") {//Custom error sent from server
                        this.setState({
                            error: [], //Empties the errors from validation
                            serverError: data.message
                        });
                    } else if (data.status == "success") {
                        window.location.replace("/events");
                    }
                })
            });
        }
    }

    /* 
    Renders the Login View
    */
    render() {
        return (
            <div className="text-center py-2">
                <h1>Login</h1>
                <hr />
                <div id="Error message"><p>{this.renderErrors()}</p></div>
                <Form id="feedback">
                    {this.renderEmail()}
                    {this.renderUsername()}
                    <hr />
                    <Row>
                        <Col xs={0} sm={1} md={3}></Col>
                        <Col xs={12} sm={10} md={6}>
                            <Button className="w-100" type="button" variant="primary" onClick={this.sendStateToServer}>Submit</Button>
                        </Col>
                        <Col xs={0} sm={1} md={3}></Col>
                    </Row>
                </Form>
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