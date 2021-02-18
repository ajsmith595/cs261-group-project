import React from "react";
import { Form, Col, Row, Button } from "react-bootstrap";
export default class RegisterView extends React.Component{
    constructor(props) {
        super(props);
        this.sendStateToServer = this.sendStateToServer.bind(this);

        this.state = {
            email: "",
            username: ""
        };
    }

    sendStateToServer() {
        fetch('/api/login',
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
                <h1>Login</h1>
                <hr />
                <div id="Error message"><p>{this.state.error}</p></div>
                <Form method="POST" id="feedback" action="/">
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
}