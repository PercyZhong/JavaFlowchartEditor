package editor.action;

import editor.model.FlowchartShape;
import java.util.List;

public class AddShapeCommand implements Command {
    private List<FlowchartShape> shapes;
    private FlowchartShape shape;
    public AddShapeCommand(List<FlowchartShape> shapes, FlowchartShape shape) {
        this.shapes = shapes; this.shape = shape;
    }
    @Override
    public void execute() { 
        System.out.println("AddShapeCommand.execute called");
        shapes.add(shape); 
        System.out.println("shapes.size() after add = " + shapes.size());
    }
    @Override
    public void undo() { shapes.remove(shape); }
}