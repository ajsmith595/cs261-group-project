import React from "react";
import { Form, Col, Row, Button } from "react-bootstrap";
// import '../feedback.css';
export default class FeedbackView extends React.Component {
    constructor(props) {
        super(props);
        console.log(props.data);
        this.sendStateToServer = this.sendStateToServer.bind(this);
        this.state = {
            status: 'show',
            anonymous: false,
            questions: props.data.questions
        };


    }

    /*componentDidMount() {
        fetch('/api/event/:id')
          .then((response) => response.json())
          .then((data) => this.setState({questions: data, loading: false}));
    }*/


    // Send the feedback to the server
    sendStateToServer() {
        this.setState({
            status: 'loading'
        });
        fetch((process.env.REACT_APP_HTTP_ADDRESS || "") + `/api/event/${this.props.eventID}/feedback`,
            {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(this.state),
            }
        ).then(response => response.json()).then(data => {
            if (data.status === 'success') {
                // Reset all the values of all the questions, so if they want to submit again, the question values are not identical
                for (let i in this.state.questions) {
                    let question = this.state.questions[i];

                    if (question.type === "open") {
                        question.value = "";
                    }
                    else if (question.type === "choice") {
                        question.value = -1;
                    }
                    else if (question.type === "numeric") {
                        question.value = Math.round((question.min + question.max) / 2);
                    }
                }
                this.setState({
                    status: 'success',
                    questions: this.state.questions
                });
            }
            else {
                this.setState({
                    status: 'error'
                })
            }
        }).catch(err => {
            this.setState({
                status: 'error'
            })
        });
    }

    render() {
        if (this.state.status === 'loading') {
            return (
                <h1 className="text-center">Loading...</h1>
            );
        }
        else if (this.state.status === 'success') {
            return (
                <Row>
                    <Col xs={0} sm={1} md={3} />
                    <Col xs={12} sm={10} md={6}>
                        <h1 className="text-center">Feedback Successful!</h1>
                        <Button className="w-100 mx-auto" variant="primary" onClick={() => this.setState({ status: 'show' })}>Give more feedback</Button>
                    </Col>
                    <Col xs={0} sm={1} md={3} />
                </Row>
            );
        }
        else if (this.state.status === 'error') {
            return (
                <Row>
                    <Col xs={0} sm={1} md={3} />
                    <Col xs={12} sm={10} md={6}>
                        <h1 className="text-center">An error occurred!</h1>
                        <Button className="w-100 mx-auto" variant="primary" onClick={() => this.setState({ status: 'show' })}>Try Again?</Button>
                    </Col>
                    <Col xs={0} sm={1} md={3} />
                </Row>
            );
        }
        let questions = [];
        for (let id in this.state.questions) {
            let question = this.state.questions[id];
            switch (question.type) {
                case 'open':
                    questions.push(this.renderLongQuestion(question));
                    break;
                case 'numeric':
                    questions.push(this.renderRangeQuestion(question));
                    break;
                case 'choice':
                    questions.push(this.renderMultipleChoiceQuestion(question));
                    break;
            }
            questions.push(<hr />);
        }
        return (
            <div className="text-center py-2">
                <h1>Feedback</h1>
                <hr />
                <Form onSubmit={this.sendStateToServer}>
                    {questions}
                    <Form.Check type="checkbox" id="anonymous_check" >
                        <Form.Check.Input type="checkbox" checked={this.state.anonymous} onChange={(e) => this.setState({ anonymous: e.target.checked })} />
                        <Form.Check.Label>Submit the feedback anonymously (the host will not see your username)</Form.Check.Label>
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

    changeQuestion(id) {
        let func = (e) => {
            let questions = this.state.questions;
            questions[id].value = e.target.value;
            this.setState({
                questions: questions
            });
        };
        return func.bind(this);
    }

    renderLongQuestion(question) {
        return (
            <Form.Group controlId={"formQuestion_" + question.id}>
                <Form.Label>{question.title}</Form.Label>
                <Row>
                    <Col xs={0} sm={1} md={3}></Col>
                    <Col xs={12} sm={10} md={6}>
                        <Form.Control className="mx-auto" onChange={this.changeQuestion(question.id)} value={question.value} placeholder="Enter answer here" as="textarea" rows={4}></Form.Control>
                    </Col>
                    <Col xs={0} sm={1} md={3}></Col>
                </Row>
            </Form.Group >
        );
    }

    renderRangeQuestion(question) {
        return (
            <Form.Group controlId={"formQuestion_" + question.id}>
                <Form.Label>{question.title}</Form.Label>

                <Row>
                    <Col xs={0} sm={1} md={3}></Col>
                    <Col xs={12} sm={10} md={6}>
                        <Form.Control className="mx-auto" value={question.value} onChange={this.changeQuestion(question.id)} type="range" min={question.min} max={question.max} step="1"></Form.Control>
                        <small className="text-center text-muted">{question.value}</small>
                    </Col>
                    <Col xs={0} sm={1} md={3}></Col>
                </Row>
            </Form.Group >
        )
    }

    renderMultipleChoiceQuestion(question) {
        let options = [];
        for (let choice in question.choices) {
            options.push(<option value={choice}>{question.choices[choice]}</option>);
        }
        return (
            <Form.Group controlId={"formQuestion_" + question.id}>
                <Form.Label>{question.title}</Form.Label>
                <Row>
                    <Col xs={0} sm={1} md={3}></Col>
                    <Col xs={12} sm={10} md={6}>
                        <Form.Control className="mx-auto" as="select" value={question.value} onChange={this.changeQuestion(question.id)}>
                            <option disabled value={-1}>Choose an option...</option>
                            {options}
                        </Form.Control>
                    </Col>
                    <Col xs={0} sm={1} md={3}></Col>
                </Row>
            </Form.Group>
        )


    }
}