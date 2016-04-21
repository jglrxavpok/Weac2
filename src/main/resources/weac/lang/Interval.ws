package weac.lang

Interval {

    private Double start;
    private Double end;
    private Double step;

    Interval(Double _start, Double _end) {
        this(_start, _end, 0D);
    }

    Interval(Double _start, Double _end, Double _step) {
        this.start = _start;
        this.end = _end;
        this.step = _step;
    }

    Boolean isIn(Double value) {
        Boolean inRange = value >= start & value <= end;
        Boolean isValue = false;
        if(step == 0D) {
            isValue = true;
        } else {
            isValue = Math.isInteger((value-start)/step);
        }
        return inRange & isValue;
    }

    Interval by(Double step) {
        return new Interval(start, end, step);
    }
}