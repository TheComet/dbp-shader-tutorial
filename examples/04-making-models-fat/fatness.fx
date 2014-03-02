// ----------------------------------------------------------------------------
// Projection matrices
// ----------------------------------------------------------------------------

float4x4 matWorldViewProjection : WORLDVIEWPROJECTION;

// ----------------------------------------------------------------------------
// Shader Constants
// ----------------------------------------------------------------------------

// determines how fat the character is
float fatness = 0.0f;

// ----------------------------------------------------------------------------
// Input and output structs
// ----------------------------------------------------------------------------

struct VS_INPUT
{
	float4 position : POSITION0;
	float3 normal : NORMAL0;
};

struct VS_OUTPUT
{
	float4 position : POSITION0;
};

struct PS_INPUT
{
};

struct PS_OUTPUT
{
	float4 colour : COLOR;
};

// ----------------------------------------------------------------------------
// Vertex shader
// ----------------------------------------------------------------------------

VS_OUTPUT vs_main( VS_INPUT input )
{
	// declare output struct, so we can write output data
	VS_OUTPUT output;

	// change fatness
	float3 fatPosition = input.position.xyz + (input.normal * fatness);

	// transform new fat position into projection space
	output.position = mul( float4(fatPosition, 1.0), matWorldViewProjection );

	// return output data
	return output;
}

// ----------------------------------------------------------------------------
// Pixel shader
// ----------------------------------------------------------------------------

PS_OUTPUT ps_main( PS_INPUT input )
{
	// declare output struct, so we can write output data
	PS_OUTPUT output;

	// this is a very simple shader. colour every pixel green
	output.colour = float4( 0.0f, 1.0f, 0.0f, 1.0f );

	// return output data
	return output;
}

// ----------------------------------------------------------------------------
// Techniques
// ----------------------------------------------------------------------------

technique Default
{
	pass p0
	{
		VertexShader = compile vs_1_1 vs_main();
		PixelShader = compile ps_1_1 ps_main();
	}
}