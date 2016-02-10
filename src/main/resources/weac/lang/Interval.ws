package weac.lang

Interval {

    private Double start;
    private Double end;
    private Double step;

    Interval(Double start, Double end) {
        this(start, end, 0D);
    }

    Interval(Double start, Double end, Double step) {
        this.start = start;
        this.end = end;
        this.step = step;
    }

    Boolean isIn(Double value) {
        Boolean inRange = value >= start && value <= end;
        Boolean isValue = false;
        if(step == 0D) {
            isValue = true;
        } else {
            isValue = Math.isInteger((value-start)/step);
        }
        return inRange && isValue;
    }

    Interval by(Double step) {
        return new Interval(start, end, step);
    }
}