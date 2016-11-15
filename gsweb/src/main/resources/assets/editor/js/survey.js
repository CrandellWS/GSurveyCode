function Survey() {
    this.nodeList = new Array();
    this.seq = 1000;
    this.startNode = new VNode(1, 'Start');
    this.startNode.xpos = 1;
    this.startNode.ypos = 1;
    this.endNode = new VNode(2, 'End');
    this.endNode.xpos = 350;
    this.endNode.ypos = 1;
    this.dateCreated = (new Date).getTime();
    this.dateModified = (new Date).getTime();
    this.status='offline';
    this.startDate = 0;
    this.endDate = 0;
    this.name = 'My GameSurvey';
    this.description = '# Welcome \n' +
        'to your GameSurvey. \n' +
        'You can print text in *Italic*, **bold**, and `monospace`. \n' +
        '\n' +
        'Here\'s a list: \n' +
        '* first item \n' +
        '* second item \n' +
        '* third item \n' +
        ' \n' +
        'and images can be specified like so: \n' +
        '![example image](http://gamesurvey.io/wp-content/uploads/2016/01/Gamesurvey-Logo-Transparent-1.png "An exemplary image") \n';
    this.contact = '';
}


function VNode(_id, _name) {
    this.id = _id; //set from sequence number
    this.name = _name;
    this.uuid = ''; //video uuid
    this.area = Create2DArray(32, 32, 0);
    this.result = Create1DArray(8, ''); //value written to user csv db
    this.action = Create1DArray(8, ''); //stores name of next node
    this.xpos = 0;
    this.ypos = 0;
}

function Create2DArray(rows, cols, init) {
    var arr = [];
    for (var i = 0; i < rows; i++) {
        arr[i] = [];
        for (var j = 0; j < cols; j++) arr[i][j] = init;
    }
    return arr;
}

function Create1DArray(rows, init) {
    var arr = [];
    for (var j = 0; j < rows; j++) arr[j] = init;
    return arr;
}