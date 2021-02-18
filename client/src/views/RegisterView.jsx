import React from "react";
import { Form, Col, Row, Button } from "react-bootstrap";
import { Redirect } from "react-router-dom";
export default class RegisterView extends React.Component{
    constructor(props) {
        super(props);
        this.sendStateToServer = this.sendStateToServer.bind(this);

        this.state = {
            acceptTerms: false,
            email: "",
            username: "",
            error: ""
        };
    }

    sendStateToServer() {
        fetch('/api/register',
        {
          method: 'POST',
          headers: {
            'Accept': 'application/json',
            'Content-Type': 'application/json'
          },
          body: JSON.stringify(this.state),
        }
        ).then(response => {
            if (response.status >= 400) {
                throw new Error("Bad response from server");
            }
            response.json().then(data => {
              if(data.status == "error"){
                  this.setState({
                    error: data.message
                  });
              }else if(data.status == "success"){
                window.location.replace("/events");
              }
            })
        });
    }

    render(){
        return (
            <div className="text-center py-2">
                <h1>Register</h1>
                <hr />
                <div id="Error message"><p>{this.state.error}</p></div>
                <p>
                    Please register your email. This will only be used for identification purposes. If you choose 
                    to be anonymous when providing feedback, your username will not be visable to the host or any
                    attendees. Your email will not be visable to any other users.
                </p>
                <Form method="POST" id="feedback" action="/register">
                    {this.renderEmail()}
                    {this.renderUsername()}
                    <Form.Check type="checkbox" id="agree_check" >
                        <Form.Check.Input type="checkbox" checked={this.state.anonymous} onChange={(e) => this.setState({ acceptTerms: e.target.checked })} />
                        <Form.Check.Label>I agree to the terms.</Form.Check.Label>
                    </Form.Check>
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
}