package pl.pawelszopinski.todoapp;

import fi.iki.elonen.NanoHTTPD;
import pl.pawelszopinski.todoapp.repository.TaskRepository;

class AppServer extends NanoHTTPD {

    private final RequestUrlMapper requestUrlMapper;

    AppServer(int port, TaskRepository taskRepository) {
        super(port);
        this.requestUrlMapper = new RequestUrlMapper(taskRepository);
    }

    @Override
    public Response serve(IHTTPSession session) {
        return requestUrlMapper.delegateRequest(session);
    }
}
