
(function(){
    $(document).ready(function(){
        //var stream = new NodecopterStream($("#droneStream")[0]);

        $("#takeoff").click(function (){ $.get("drone/takeoff"); });
        $("#land").click(function (){ $.get("drone/land"); });
        $("#stop").click(function (){ $.get("drone/stop"); });
        $("#disableEmergency").click(function (){ $.get("drone/disableEmergency"); });
        $("#up").click(function (){ $.get("drone/up/0.5"); });
        $("#down").click(function (){ $.get("drone/down/0.5"); });
        $("#front").click(function (){ $.get("drone/front/0.5"); });
        $("#back").click(function (){ $.get("drone/back/0.5"); });
        $("#left").click(function (){ $.get("drone/left/0.5"); });
        $("#right").click(function (){ $.get("drone/right/0.5"); });
        $("#clockwise").click(function (){ $.get("drone/clockwise/0.5"); });
        $("#counterClockwise").click(function (){ $.get("drone/counterClockwise/0.5"); });

        setInterval(requestNavdata, 100);
        setInterval(requestImage, 100);
    });

    function requestImage(){
        if($("#updateImage").prop("checked")) {
            $("#droneStream").attr({
                src: "drone/image?" + Math.random()
            });
        }
    }

    function requestNavdata(){
        if($("#updateNavdata").prop("checked")) {
            $.getJSON("drone/navdata/", function(data){
                $("#batteryPercentage").html(data.demo.batteryPercentage);
                $("#emergencyLanding").html(data.droneState.emergencyLanding);
                $("#controlState").html(data.demo.controlState);
                $("#flyState").html(data.demo.flyState);
                $("#clockwiseDegrees").html(data.demo.clockwiseDegrees);
                $("#frontBackDegrees").html(data.demo.frontBackDegrees);
                $("#leftRightDegrees").html(data.demo.leftRightDegrees);
                $("#altitudeMeters").html(data.demo.altitudeMeters);
                $("#xVelocity").html(data.demo.xVelocity);
                $("#yVelocity").html(data.demo.yVelocity);
                $("#zVelocity").html(data.demo.zVelocity);
            });
        }
    }

})();