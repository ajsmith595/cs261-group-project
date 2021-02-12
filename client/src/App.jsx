import React from 'react';
import EventsView from './views/EventsView.jsx';
import CreateEventView from './views/CreateEventView.jsx';
import FeedbackView from './views/FeedbackView.jsx';
import HomeView from './views/HomeView.jsx';
import HostView from './views/HostView.jsx';
import RegisterView from './views/RegisterView.jsx';
import {
    BrowserRouter as Router,
    Switch,
    Route,
    Link
} from "react-router-dom";
import 'bootstrap/dist/css/bootstrap.min.css';
import { Container, Jumbotron } from 'react-bootstrap';

function App() {
    return (
        <Jumbotron className="py-1 my-0">
            <div className="border border bg-white rounded container-fluid">
                <Router>

                    <div>
                        <Link to="/event/host">Host View</Link>
                    </div>
                    <Switch>
                        <Route path="/events">
                            <EventsView />
                        </Route>
                        <Route path="/event/host">
                            <HostView />
                        </Route>
                        <Route path="/event/attendee">
                            <FeedbackView />
                        </Route>
                    </Switch>
                </Router>
            </div>
        </Jumbotron>
    );
}

export default App;
