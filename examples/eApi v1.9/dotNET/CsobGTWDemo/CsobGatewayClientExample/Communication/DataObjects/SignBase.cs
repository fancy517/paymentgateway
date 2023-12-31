﻿using Newtonsoft.Json;

namespace CsobGatewayClientExample.Communication.DataObjects;

public abstract class SignBase : ApiBase
{
    [JsonProperty("dttm")] public string? Dttm { get; set; }

    [JsonProperty("signature")] public string? Signature { get; set; }

    public abstract string ToSign();
}